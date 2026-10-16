package com.skillSwap.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillSwap.Dto.studyroom.StudyRoomDtos.ChatMessage;
import com.skillSwap.Service.StudyRoomAttendanceService;
import com.skillSwap.Service.StudyRoomChatService;
import com.skillSwap.Service.StudyRoomService;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Live connection for ONE room: /ws/rooms?roomId=5&token=...
 * Browser -> server frames:  {"type":"chat","content":"hi","clientId":"..."}  {"type":"heartbeat","visible":true}  {"type":"ping"}
 * Server -> browser frames:  {"type":"chat|chat_deleted|presence|timer|notes|challenge|room|reward|kicked|error","data":{...}}
 * Who the sender is and whether they may post is decided here and in the services, never by the browser.
 */
@Component
public class StudyRoomWebSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(StudyRoomWebSocketHandler.class);
	private static final int MAX_FRAME_BYTES = 4096;
	private static final int CHAT_LIMIT = 5; // messages ...
	private static final long CHAT_WINDOW_MS = 5000; // ... per 5 seconds per user
	private static final Pattern CLIENT_ID = Pattern.compile("^[A-Za-z0-9-]{8,36}$");
	private static final String SAFE = "safeSession";

	private final StudyRoomPresenceRegistry registry;
	private final StudyRoomService roomService;
	private final StudyRoomChatService chatService;
	private final StudyRoomAttendanceService attendance;
	private final ObjectMapper mapper;
	private final Map<Integer, Deque<Long>> chatTimes = new ConcurrentHashMap<>();

	public StudyRoomWebSocketHandler(
		StudyRoomPresenceRegistry registry,
		StudyRoomService roomService,
		StudyRoomChatService chatService,
		StudyRoomAttendanceService attendance,
		ObjectMapper mapper
	) {
		this.registry = registry;
		this.roomService = roomService;
		this.chatService = chatService;
		this.attendance = attendance;
		this.mapper = mapper;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession raw) {
		// several threads may send to one session (chat + timer + presence): the decorator serialises them
		WebSocketSession session = new ConcurrentWebSocketSessionDecorator(raw, 5000, 64 * 1024);
		raw.getAttributes().put(SAFE, session);
		Integer userId = userId(raw);
		Integer roomId = roomId(raw);
		try {
			var info = roomService.socketInfo(roomId);
			var result = registry.tryJoin(roomId, userId, session, info.maxParticipants());
			if (result == StudyRoomPresenceRegistry.JoinResult.FULL) {
				registry.sendTo(session, "error", Map.of("message", "This room is full"));
				registry.close(session, new CloseStatus(StudyRoomPresenceRegistry.CLOSE_FULL, "full"));
				return;
			}
			if (result == StudyRoomPresenceRegistry.JoinResult.NEW_USER) {
				roomService.broadcastPresence(roomId);
			} else {
				registry.sendTo(session, "presence", roomService.presence(roomId));
			}
		} catch (RuntimeException e) {
			log.warn("Could not open room socket: {}", e.getMessage());
			registry.close(session, CloseStatus.SERVER_ERROR);
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession raw, TextMessage message) {
		WebSocketSession session = safe(raw);
		if (message.getPayloadLength() > MAX_FRAME_BYTES) {
			registry.close(session, CloseStatus.TOO_BIG_TO_PROCESS);
			return;
		}
		Integer userId = userId(raw);
		Integer roomId = roomId(raw);
		JsonNode n;
		try {
			n = mapper.readTree(message.getPayload());
		} catch (IOException e) {
			registry.sendTo(session, "error", Map.of("message", "Invalid message"));
			return;
		}
		switch (n.path("type").asText("")) {
			case "chat" -> handleChat(session, roomId, userId, n);
			case "heartbeat" -> handleHeartbeat(session, roomId, userId, n.path("visible").asBoolean(true));
			case "ping" -> registry.sendTo(session, "pong", Map.of());
			default -> registry.sendTo(session, "error", Map.of("message", "Unknown message type"));
		}
	}

	private void handleChat(WebSocketSession session, Integer roomId, Integer userId, JsonNode n) {
		String clientId = n.path("clientId").asText(null);
		if (clientId != null && !CLIENT_ID.matcher(clientId).matches()) clientId = null;
		if (!allowChat(userId)) {
			registry.sendTo(
				session,
				"error",
				Map.of("message", "You are sending messages too fast", "clientId", String.valueOf(clientId))
			);
			return;
		}
		try {
			ChatMessage saved = chatService.send(roomId, userId, n.path("content").asText(""), clientId);
			registry.broadcast(roomId, "chat", saved); // everyone, including the sender (that is their delivery confirmation)
		} catch (IllegalArgumentException | org.springframework.security.access.AccessDeniedException e) {
			registry.sendTo(session, "error", Map.of("message", e.getMessage(), "clientId", String.valueOf(clientId)));
		} catch (RuntimeException e) {
			log.warn("Chat message failed in room {}: {}", roomId, e.getClass().getSimpleName()); // never log the message text
			registry.sendTo(
				session,
				"error",
				Map.of("message", "Could not send message", "clientId", String.valueOf(clientId))
			);
		}
	}

	private void handleHeartbeat(WebSocketSession session, Integer roomId, Integer userId, boolean visible) {
		registry.beat(session.getId());
		try {
			attendance.onHeartbeat(roomId, userId, visible, registry.count(roomId));
		} catch (RuntimeException e) {
			log.debug("Attendance heartbeat skipped: {}", e.getMessage());
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession raw, CloseStatus status) {
		registry
			.leave(raw.getId())
			.ifPresent(left -> {
				if (!left.userGone()) return;
				try {
					attendance.noteLeave(left.roomId(), left.userId());
					roomService.broadcastPresence(left.roomId());
				} catch (RuntimeException e) {
					log.debug("Leave bookkeeping failed: {}", e.getMessage());
				}
			});
	}

	@Override
	public void handleTransportError(WebSocketSession raw, Throwable exception) {
		registry.close(safe(raw), CloseStatus.SERVER_ERROR);
	}

	private boolean allowChat(Integer userId) {
		Deque<Long> times = chatTimes.computeIfAbsent(userId, k -> new ArrayDeque<>());
		long now = System.currentTimeMillis();
		synchronized (times) {
			while (!times.isEmpty() && now - times.peekFirst() > CHAT_WINDOW_MS) times.pollFirst();
			if (times.size() >= CHAT_LIMIT) return false;
			times.addLast(now);
			return true;
		}
	}

	private WebSocketSession safe(WebSocketSession raw) {
		Object s = raw.getAttributes().get(SAFE);
		return s instanceof WebSocketSession w ? w : raw;
	}

	private Integer userId(WebSocketSession s) {
		return (Integer) s.getAttributes().get(JwtHandshakeInterceptor.USER_ID);
	}

	private Integer roomId(WebSocketSession s) {
		return (Integer) s.getAttributes().get(StudyRoomHandshakeInterceptor.ROOM_ID);
	}
}
