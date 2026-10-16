package com.skillSwap.Service;

import com.skillSwap.Dto.studyroom.StudyRoomDtos.NoteEvent;
import com.skillSwap.Dto.studyroom.StudyRoomDtos.NoteView;
import com.skillSwap.Entity.StudyRoomNote;
import com.skillSwap.Exception.ResourceNotFoundException;
import com.skillSwap.Exception.StudyRoomException;
import com.skillSwap.Repository.StudyRoomNoteRepository;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.websocket.StudyRoomPresenceRegistry;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shared notes with "optimistic locking": every save says which version it started from.
 * If somebody saved in between, the save is refused (409) instead of silently overwriting their work.
 * This is NOT live simultaneous editing (like Google Docs) - people take turns, and conflicts are shown to the user.
 */
@Service
public class StudyRoomNoteService {

	private final StudyRoomNoteRepository notes;
	private final StudyRoomService roomService;
	private final UserRepository users;
	private final StudyRoomPresenceRegistry registry;

	public StudyRoomNoteService(
		StudyRoomNoteRepository notes,
		StudyRoomService roomService,
		UserRepository users,
		StudyRoomPresenceRegistry registry
	) {
		this.notes = notes;
		this.roomService = roomService;
		this.users = users;
		this.registry = registry;
	}

	@Transactional(readOnly = true)
	public NoteView get(Integer userId, Integer roomId) {
		roomService.requireRoom(roomId);
		roomService.requireActiveMember(roomId, userId);
		return toView(load(roomId));
	}

	@Transactional
	public NoteView save(Integer userId, Integer roomId, String content, long baseVersion) {
		roomService.requireRoom(roomId);
		roomService.requireActiveMember(roomId, userId);
		int changed = notes.saveIfVersionMatches(
			roomId,
			content,
			users.getReferenceById(userId),
			Instant.now(),
			baseVersion
		);
		if (changed == 0) {
			throw new StudyRoomException(
				HttpStatus.CONFLICT,
				"Someone else saved these notes first. Load the latest version."
			);
		}
		NoteView v = toView(load(roomId));
		registry.broadcastAfterCommit(
			roomId,
			"notes",
			new NoteEvent(v.version(), v.updatedById(), v.updatedByName(), v.updatedAt())
		);
		return v;
	}

	private StudyRoomNote load(Integer roomId) {
		return notes.findByRoomId(roomId).orElseThrow(() -> new ResourceNotFoundException("Notes", "roomId", roomId));
	}

	private NoteView toView(StudyRoomNote n) {
		return new NoteView(
			n.getContent(),
			n.getRevision(),
			n.getUpdatedBy() == null ? null : n.getUpdatedBy().getId(),
			n.getUpdatedBy() == null ? null : StudyRoomService.nameOf(n.getUpdatedBy()),
			n.getUpdatedAt()
		);
	}
}
