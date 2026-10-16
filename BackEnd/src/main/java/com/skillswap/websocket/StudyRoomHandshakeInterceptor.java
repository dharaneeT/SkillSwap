package com.skillSwap.websocket;

import com.skillSwap.Service.StudyRoomService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Runs AFTER JwtHandshakeInterceptor (which puts the real user id in the attributes).
 * Refuses the connection unless that user is an ACTIVE member of the requested room.
 * This is the backend check that stops someone from subscribing to a room they cannot see.
 */
@Component
public class StudyRoomHandshakeInterceptor implements HandshakeInterceptor {

	public static final String ROOM_ID = "roomId";

	private final StudyRoomService roomService;

	public StudyRoomHandshakeInterceptor(StudyRoomService roomService) {
		this.roomService = roomService;
	}

	@Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler handler,
		Map<String, Object> attributes
	) {
		Integer userId = (Integer) attributes.get(JwtHandshakeInterceptor.USER_ID);
		String raw = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("roomId");
		try {
			Integer roomId = Integer.valueOf(raw);
			if (userId != null && roomService.canConnect(roomId, userId)) {
				attributes.put(ROOM_ID, roomId);
				return true;
			}
		} catch (RuntimeException ignored) {
			// bad or missing roomId falls through to "forbidden"
		}
		response.setStatusCode(HttpStatus.FORBIDDEN);
		return false;
	}

	@Override
	public void afterHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler handler,
		Exception exception
	) {}
}
