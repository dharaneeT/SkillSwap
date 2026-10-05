package com.skillSwap.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillSwap.Dto.chat.MessageRequestDTO;
import com.skillSwap.Dto.chat.MessageResponseDTO;
import com.skillSwap.Service.ChatService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
//it tells Spring that this class will handle incoming and outgoing text messages over WebSocket.
//it listens continuously for messages as long as the connection is alive.
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

	// userId -> that user's open connections (several tabs/devices allowed)
	//This map is the in-memory registry of connected users.
	//Key → userId
	//Value → all active WebSocket sessions of that user
	private final Map<Integer, Set<WebSocketSession>> online = new ConcurrentHashMap<>();

	private final ChatService chatService;
	private final ObjectMapper mapper;
	private final Validator validator;

	public ChatWebSocketHandler(ChatService chatService, ObjectMapper mapper, Validator validator) {
		this.chatService = chatService;
		this.mapper = mapper;
		this.validator = validator;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		//Get userId from session (JWT decoded earlier)
		//Add session to online map
		online.computeIfAbsent(userId(session), k -> ConcurrentHashMap.newKeySet()).add(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		//This ensures security, because users cannot fake senderId in request body
		Integer senderId = userId(session);
		try {
			//Convert JSON → DTO
			MessageRequestDTO dto = mapper.readValue(message.getPayload(), MessageRequestDTO.class);

			//Before processing, the system checks:
			//receiverId is present
			//message is not empty
			//message length is valid
			Set<ConstraintViolation<MessageRequestDTO>> errors = validator.validate(dto);
			if (!errors.isEmpty()) {
				sendError(session, errors.iterator().next().getMessage());
				return;
			}

            //This calls your ChatService, which
            //Fetches sender and receiver from DB
            //Creates a message entity
            //Stores it in MySQL
            //Returns DTO
			MessageResponseDTO saved = chatService.send(senderId, dto); // persisted to MySQL here

            //Convert Response to JSON
			String json = mapper.writeValueAsString(saved);
            //Send Message to Receiver
			push(dto.getReceiverId(), json); // deliver to receiver

            //If user has multiple tabs → all tabs sync
            //UI instantly shows sent message
			push(senderId, json); // echo to sender (all their tabs)
		} catch (JsonProcessingException e) {
			sendError(session, "Invalid JSON. Expected {\"receiverId\":2,\"content\":\"hi\"}");
		} catch (RuntimeException e) {
			sendError(session, e.getMessage() == null ? "Could not send message" : e.getMessage());
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		Set<WebSocketSession> set = online.get(userId(session));
//        Their session is removed from the map
//        If no sessions remain → user is removed completely
		if (set != null) {
			set.remove(session);
			if (set.isEmpty()) online.remove(userId(session));
		}
	}

	private Integer userId(WebSocketSession session) {
		return (Integer) session.getAttributes().get(JwtHandshakeInterceptor.USER_ID);
	}

	private void push(Integer userId, String json) {
		for (WebSocketSession s : online.getOrDefault(userId, Set.of())) {
			if (!s.isOpen()) continue;
			try {
				synchronized (s) { // WebSocketSession is not safe for concurrent sends
					s.sendMessage(new TextMessage(json));
				}
			} catch (IOException e) {
				log.warn("Could not push to user {}: {}", userId, e.getMessage());
			}
		}
	}

	private void sendError(WebSocketSession session, String error) throws IOException {
		synchronized (session) {
			session.sendMessage(new TextMessage(mapper.writeValueAsString(Map.of("error", error))));
		}
	}
}
