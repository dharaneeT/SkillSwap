package com.skillSwap.Service;

import com.skillSwap.Dto.studyroom.StudyRoomDtos.ChatMessage;
import com.skillSwap.Entity.StudyRoom;
import com.skillSwap.Entity.StudyRoomMessage;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Repository.StudyRoomMessageRepository;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.websocket.StudyRoomPresenceRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyRoomChatService {

	public static final int MAX_LENGTH = 1000;

	private final StudyRoomMessageRepository messages;
	private final StudyRoomService roomService;
	private final UserRepository users;
	private final StudyRoomPresenceRegistry registry;

	public StudyRoomChatService(
		StudyRoomMessageRepository messages,
		StudyRoomService roomService,
		UserRepository users,
		StudyRoomPresenceRegistry registry
	) {
		this.messages = messages;
		this.roomService = roomService;
		this.users = users;
		this.registry = registry;
	}

	/** The server decides who the sender is and whether they may post. The browser only supplies text. */
	@Transactional
	public ChatMessage send(Integer roomId, Integer userId, String content, String clientId) {
		String text = content == null ? "" : content.strip();
		if (text.isEmpty()) throw new IllegalArgumentException("Message cannot be empty");
		if (text.length() > MAX_LENGTH) throw new IllegalArgumentException(
			"Message is too long (max " + MAX_LENGTH + " characters)"
		);

		StudyRoom room = roomService.requireRoom(roomId);
		roomService.requireActiveMember(roomId, userId);

		if (clientId != null) {
			// a retry of a message we already saved: return it instead of saving a duplicate
			var existing = messages.findByRoom_IdAndSender_IdAndClientId(roomId, userId, clientId);
			if (existing.isPresent()) return toDto(existing.get());
		}

		StudyRoomMessage m = new StudyRoomMessage();
		m.setRoom(room);
		m.setSender(users.getReferenceById(userId));
		m.setContent(text);
		m.setClientId(clientId);
		m = messages.save(m);
		m.setSender(users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)));
		return toDto(m);
	}

	/** Oldest-to-newest. Pass the id of the oldest message you already have as "before" to load earlier ones. */
	@Transactional(readOnly = true)
	public List<ChatMessage> history(Integer userId, Integer roomId, Long before, int limit) {
		roomService.requireRoom(roomId);
		roomService.requireActiveMember(roomId, userId);
		int size = Math.max(1, Math.min(limit, 100));
		List<ChatMessage> page = new ArrayList<>(
			messages
				.findPage(roomId, before == null ? Long.MAX_VALUE : before, PageRequest.of(0, size))
				.stream()
				.map(this::toDto)
				.toList()
		);
		Collections.reverse(page);
		return page;
	}

	/** Host moderation: hides the message for everyone (soft delete). */
	@Transactional
	public void delete(Integer hostId, Integer roomId, Long messageId) {
		roomService.requireHost(roomId, hostId);
		StudyRoomMessage m = messages
			.findByIdAndRoom_Id(messageId, roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));
		m.setDeleted(true);
		registry.broadcastAfterCommit(roomId, "chat_deleted", Map.of("id", messageId));
	}

	private ChatMessage toDto(StudyRoomMessage m) {
		return new ChatMessage(
			m.getId(),
			m.getClientId(),
			m.getSender().getId(),
			StudyRoomService.nameOf(m.getSender()),
			m.getContent(),
			m.getCreatedAt()
		);
	}
}
