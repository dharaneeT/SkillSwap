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
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    // userId -> that user's open connections (several tabs/devices allowed)
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
        online.computeIfAbsent(userId(session), k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Integer senderId = userId(session);
        try {
            MessageRequestDTO dto = mapper.readValue(message.getPayload(), MessageRequestDTO.class);

            Set<ConstraintViolation<MessageRequestDTO>> errors = validator.validate(dto);
            if (!errors.isEmpty()) {
                sendError(session, errors.iterator().next().getMessage());
                return;
            }

            MessageResponseDTO saved = chatService.send(senderId, dto);   // persisted to MySQL here
            String json = mapper.writeValueAsString(saved);
            push(dto.getReceiverId(), json);   // deliver to receiver
            push(senderId, json);              // echo to sender (all their tabs)
        } catch (JsonProcessingException e) {
            sendError(session, "Invalid JSON. Expected {\"receiverId\":2,\"content\":\"hi\"}");
        } catch (RuntimeException e) {
            sendError(session, e.getMessage() == null ? "Could not send message" : e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Set<WebSocketSession> set = online.get(userId(session));
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
                synchronized (s) {                    // WebSocketSession is not safe for concurrent sends
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