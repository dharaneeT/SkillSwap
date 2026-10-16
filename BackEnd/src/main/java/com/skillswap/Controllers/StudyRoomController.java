package com.skillSwap.Controllers;

import com.skillSwap.Dto.common.PageResponse;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.studyroom.StudyRoomDtos.*;
import com.skillSwap.Entity.StudyRoomEnums.Difficulty;
import com.skillSwap.Security.CurrentUserService;
import com.skillSwap.Service.StudyRoomChatService;
import com.skillSwap.Service.StudyRoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Rooms, membership, sessions, chat history. The user is ALWAYS taken from the JWT, never from the request. */
@RestController
@RequestMapping("/skillswap/v1/study-rooms")
@Tag(name = "Study Rooms API", description = "Virtual study rooms")
public class StudyRoomController {

	private final StudyRoomService rooms;
	private final StudyRoomChatService chat;
	private final CurrentUserService currentUser;

	public StudyRoomController(StudyRoomService rooms, StudyRoomChatService chat, CurrentUserService currentUser) {
		this.rooms = rooms;
		this.chat = chat;
		this.currentUser = currentUser;
	}

	private Integer me() {
		return currentUser.getCurrentUser().getId();
	}

	private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
		return ResponseEntity.ok(new ApiResponse<>(true, message, data));
	}

	// GET /study-rooms?q=&topic=&difficulty=&filter=ALL|AVAILABLE|ACTIVE|SCHEDULED&page=0&size=12
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<RoomCard>>> list(
		@RequestParam(required = false) String q,
		@RequestParam(required = false) String topic,
		@RequestParam(required = false) Difficulty difficulty,
		@RequestParam(defaultValue = "ALL") String filter,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "12") int size
	) {
		return ok("Study rooms", rooms.list(me(), q, topic, difficulty, filter, page, size));
	}

	@PostMapping
	public ResponseEntity<ApiResponse<RoomDetail>> create(@Valid @RequestBody RoomRequest req) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(new ApiResponse<>(true, "Room created", rooms.create(me(), req)));
	}

	@GetMapping("/{roomId}")
	public ResponseEntity<ApiResponse<RoomDetail>> detail(@PathVariable Integer roomId) {
		return ok("Study room", rooms.detail(me(), roomId));
	}

	@PutMapping("/{roomId}")
	public ResponseEntity<ApiResponse<RoomDetail>> update(
		@PathVariable Integer roomId,
		@Valid @RequestBody RoomRequest req
	) {
		return ok("Room updated", rooms.update(me(), roomId, req));
	}

	// "delete" = archive
	@DeleteMapping("/{roomId}")
	public ResponseEntity<ApiResponse<String>> archive(@PathVariable Integer roomId) {
		rooms.archive(me(), roomId);
		return ok("Room archived", "OK");
	}

	@PostMapping("/{roomId}/join")
	public ResponseEntity<ApiResponse<RoomDetail>> join(
		@PathVariable Integer roomId,
		@Valid @RequestBody(required = false) JoinRequest req
	) {
		return ok("Joined", rooms.join(me(), roomId, req == null ? null : req.accessCode()));
	}

	@PostMapping("/{roomId}/leave")
	public ResponseEntity<ApiResponse<String>> leave(@PathVariable Integer roomId) {
		rooms.leave(me(), roomId);
		return ok("Left room", "OK");
	}

	@GetMapping("/{roomId}/presence")
	public ResponseEntity<ApiResponse<List<PresenceEntry>>> presence(@PathVariable Integer roomId) {
		rooms.requireActiveMember(roomId, me());
		return ok("Who is here", rooms.presence(roomId));
	}

	// ---- host: members ----

	@GetMapping("/{roomId}/members")
	public ResponseEntity<ApiResponse<List<MemberView>>> members(@PathVariable Integer roomId) {
		return ok("Members", rooms.members(me(), roomId));
	}

	@DeleteMapping("/{roomId}/members/{userId}")
	public ResponseEntity<ApiResponse<String>> remove(@PathVariable Integer roomId, @PathVariable Integer userId) {
		rooms.removeMember(me(), roomId, userId);
		return ok("Member removed", "OK");
	}

	@PostMapping("/{roomId}/members/{userId}/restore")
	public ResponseEntity<ApiResponse<String>> restore(@PathVariable Integer roomId, @PathVariable Integer userId) {
		rooms.restoreMember(me(), roomId, userId);
		return ok("Member restored", "OK");
	}

	// ---- host: sessions ----

	@PostMapping("/{roomId}/session/start")
	public ResponseEntity<ApiResponse<RoomDetail>> start(@PathVariable Integer roomId) {
		return ok("Session started", rooms.startSession(me(), roomId));
	}

	@PostMapping("/{roomId}/session/end")
	public ResponseEntity<ApiResponse<RoomDetail>> end(@PathVariable Integer roomId) {
		return ok("Session ended", rooms.endSession(me(), roomId));
	}

	// ---- chat history (sending happens over the websocket) ----

	@GetMapping("/{roomId}/messages")
	public ResponseEntity<ApiResponse<List<ChatMessage>>> messages(
		@PathVariable Integer roomId,
		@RequestParam(required = false) Long before,
		@RequestParam(defaultValue = "50") int limit
	) {
		return ok("Messages", chat.history(me(), roomId, before, limit));
	}

	@DeleteMapping("/{roomId}/messages/{messageId}")
	public ResponseEntity<ApiResponse<String>> deleteMessage(
		@PathVariable Integer roomId,
		@PathVariable Long messageId
	) {
		chat.delete(me(), roomId, messageId);
		return ok("Message removed", "OK");
	}
}
