package com.skillSwap.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Who is connected to which room RIGHT NOW (in memory), plus the "send to everyone in a room" helper.
 * A user is counted once per room even with several tabs open. Stale connections are removed by the scheduler.
 */
@Component
public class StudyRoomPresenceRegistry {

	private static final Logger log = LoggerFactory.getLogger(StudyRoomPresenceRegistry.class);

	public static final int CLOSE_FORBIDDEN = 4403; // kicked / no access: client must NOT reconnect
	public static final int CLOSE_FULL = 4409; // room is full: client must NOT reconnect
	public static final int CLOSE_ROOM_CLOSED = 4410; // room archived

	public enum JoinResult {
		NEW_USER,
		EXISTING_USER,
		FULL
	}

	public record Left(Integer roomId, Integer userId, boolean userGone, WebSocketSession session) {}

	private record Conn(Integer roomId, Integer userId, WebSocketSession session) {}

	// everything below (except lastBeat) is guarded by "synchronized"
	private final Map<String, Conn> bySession = new HashMap<>();
	private final Map<Integer, Map<Integer, Set<String>>> rooms = new HashMap<>(); // room -> user -> session ids
	private final Map<String, Long> lastBeat = new ConcurrentHashMap<>();
	private final ObjectMapper mapper;

	public StudyRoomPresenceRegistry(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	// ---------------- membership of the live room ----------------

	public synchronized JoinResult tryJoin(
		Integer roomId,
		Integer userId,
		WebSocketSession session,
		int maxParticipants
	) {
		Map<Integer, Set<String>> users = rooms.computeIfAbsent(roomId, k -> new HashMap<>());
		boolean already = users.containsKey(userId);
		if (!already && users.size() >= maxParticipants) {
			if (users.isEmpty()) rooms.remove(roomId);
			return JoinResult.FULL;
		}
		users.computeIfAbsent(userId, k -> new HashSet<>()).add(session.getId());
		bySession.put(session.getId(), new Conn(roomId, userId, session));
		lastBeat.put(session.getId(), System.currentTimeMillis());
		return already ? JoinResult.EXISTING_USER : JoinResult.NEW_USER;
	}

	/** Safe to call twice for the same session. */
	public synchronized Optional<Left> leave(String sessionId) {
		lastBeat.remove(sessionId);
		Conn c = bySession.remove(sessionId);
		if (c == null) return Optional.empty();
		Map<Integer, Set<String>> users = rooms.get(c.roomId());
		boolean gone = false;
		if (users != null) {
			Set<String> ids = users.get(c.userId());
			if (ids != null) {
				ids.remove(sessionId);
				if (ids.isEmpty()) {
					users.remove(c.userId());
					gone = true;
				}
			}
			if (users.isEmpty()) rooms.remove(c.roomId());
		}
		return Optional.of(new Left(c.roomId(), c.userId(), gone, c.session()));
	}

	public void beat(String sessionId) {
		lastBeat.computeIfPresent(sessionId, (k, v) -> System.currentTimeMillis());
	}

	/** Removes every connection that has not sent a heartbeat since cutoffMillis. */
	public synchronized List<Left> removeStale(long cutoffMillis) {
		List<String> stale = new ArrayList<>();
		for (String id : new ArrayList<>(bySession.keySet())) {
			if (lastBeat.getOrDefault(id, 0L) < cutoffMillis) stale.add(id);
		}
		List<Left> out = new ArrayList<>();
		for (String id : stale) leave(id).ifPresent(out::add);
		return out;
	}

	public synchronized int count(Integer roomId) {
		Map<Integer, Set<String>> users = rooms.get(roomId);
		return users == null ? 0 : users.size();
	}

	public synchronized Set<Integer> userIds(Integer roomId) {
		Map<Integer, Set<String>> users = rooms.get(roomId);
		return users == null ? Set.of() : new HashSet<>(users.keySet());
	}

	private synchronized List<WebSocketSession> sessionsOfRoom(Integer roomId) {
		List<WebSocketSession> out = new ArrayList<>();
		for (Conn c : bySession.values()) if (c.roomId().equals(roomId)) out.add(c.session());
		return out;
	}

	private synchronized List<WebSocketSession> sessionsOfUser(Integer roomId, Integer userId) {
		List<WebSocketSession> out = new ArrayList<>();
		for (Conn c : bySession.values()) {
			if (c.roomId().equals(roomId) && c.userId().equals(userId)) out.add(c.session());
		}
		return out;
	}

	// ---------------- sending ----------------

	public void broadcast(Integer roomId, String type, Object payload) {
		String json = toJson(type, payload);
		if (json == null) return;
		for (WebSocketSession s : sessionsOfRoom(roomId)) send(s, json);
	}

	public void sendTo(WebSocketSession session, String type, Object payload) {
		String json = toJson(type, payload);
		if (json != null) send(session, json);
	}

	public void sendToUser(Integer roomId, Integer userId, String type, Object payload) {
		String json = toJson(type, payload);
		if (json == null) return;
		for (WebSocketSession s : sessionsOfUser(roomId, userId)) send(s, json);
	}

	/** Waits for the database transaction to commit first, so nobody is told about something that gets rolled back. */
	public void broadcastAfterCommit(Integer roomId, String type, Object payload) {
		runAfterCommit(() -> broadcast(roomId, type, payload));
	}

	public void sendToUserAfterCommit(Integer roomId, Integer userId, String type, Object payload) {
		runAfterCommit(() -> sendToUser(roomId, userId, type, payload));
	}

	private void runAfterCommit(Runnable task) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(
				new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						task.run();
					}
				}
			);
		} else {
			task.run();
		}
	}

	// ---------------- removing people ----------------

	public void kickUser(Integer roomId, Integer userId) {
		for (WebSocketSession s : sessionsOfUser(roomId, userId)) {
			sendTo(s, "kicked", Map.of("reason", "You were removed from this room"));
			close(s, new CloseStatus(CLOSE_FORBIDDEN, "removed"));
		}
	}

	public void closeRoom(Integer roomId) {
		for (WebSocketSession s : sessionsOfRoom(roomId)) close(s, new CloseStatus(CLOSE_ROOM_CLOSED, "room closed"));
	}

	public void close(WebSocketSession s, CloseStatus status) {
		try {
			s.close(status);
		} catch (IOException | RuntimeException e) {
			log.debug("Could not close websocket session: {}", e.getMessage());
		}
	}

	private void send(WebSocketSession s, String json) {
		if (!s.isOpen()) return;
		try {
			s.sendMessage(new TextMessage(json)); // sessions are wrapped in ConcurrentWebSocketSessionDecorator
		} catch (IOException | RuntimeException e) {
			log.debug("Dropping slow/broken websocket session {}: {}", s.getId(), e.getMessage());
			close(s, CloseStatus.SESSION_NOT_RELIABLE);
		}
	}

	private String toJson(String type, Object payload) {
		try {
			Map<String, Object> frame = new LinkedHashMap<>();
			frame.put("type", type);
			frame.put("data", payload);
			return mapper.writeValueAsString(frame);
		} catch (IOException e) {
			log.warn("Could not serialise websocket frame '{}'", type);
			return null;
		}
	}
}
