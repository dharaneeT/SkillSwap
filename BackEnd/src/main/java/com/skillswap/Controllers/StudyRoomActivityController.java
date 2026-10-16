package com.skillSwap.Controllers;

import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.studyroom.StudyRoomDtos.*;
import com.skillSwap.Security.CurrentUserService;
import com.skillSwap.Service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Timer, notes, challenges and attendance of a room. */
@RestController
@RequestMapping("/skillswap/v1/study-rooms/{roomId}")
@Tag(name = "Study Room Activity API", description = "Timer, notes, challenges, attendance")
public class StudyRoomActivityController {

	private final StudyRoomTimerService timer;
	private final StudyRoomNoteService notes;
	private final StudyRoomChallengeService challenges;
	private final StudyRoomAttendanceService attendance;
	private final StudyRoomService rooms;
	private final CurrentUserService currentUser;

	public StudyRoomActivityController(
		StudyRoomTimerService timer,
		StudyRoomNoteService notes,
		StudyRoomChallengeService challenges,
		StudyRoomAttendanceService attendance,
		StudyRoomService rooms,
		CurrentUserService currentUser
	) {
		this.timer = timer;
		this.notes = notes;
		this.challenges = challenges;
		this.attendance = attendance;
		this.rooms = rooms;
		this.currentUser = currentUser;
	}

	private Integer me() {
		return currentUser.getCurrentUser().getId();
	}

	private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
		return ResponseEntity.ok(new ApiResponse<>(true, message, data));
	}

	// ---------------- timer ----------------

	@GetMapping("/timer")
	public ResponseEntity<ApiResponse<TimerView>> timer(@PathVariable Integer roomId) {
		return ok("Timer", timer.get(me(), roomId));
	}

	@PostMapping("/timer/start")
	public ResponseEntity<ApiResponse<TimerView>> start(
		@PathVariable Integer roomId,
		@Valid @RequestBody TimerStartRequest req
	) {
		return ok("Timer started", timer.start(me(), roomId, req.phase(), req.durationSeconds()));
	}

	@PostMapping("/timer/pause")
	public ResponseEntity<ApiResponse<TimerView>> pause(@PathVariable Integer roomId) {
		return ok("Timer paused", timer.pause(me(), roomId));
	}

	@PostMapping("/timer/resume")
	public ResponseEntity<ApiResponse<TimerView>> resume(@PathVariable Integer roomId) {
		return ok("Timer resumed", timer.resume(me(), roomId));
	}

	@PostMapping("/timer/reset")
	public ResponseEntity<ApiResponse<TimerView>> reset(@PathVariable Integer roomId) {
		return ok("Timer reset", timer.reset(me(), roomId));
	}

	// ---------------- notes ----------------

	@GetMapping("/notes")
	public ResponseEntity<ApiResponse<NoteView>> notes(@PathVariable Integer roomId) {
		return ok("Notes", notes.get(me(), roomId));
	}

	@PutMapping("/notes")
	public ResponseEntity<ApiResponse<NoteView>> saveNotes(
		@PathVariable Integer roomId,
		@Valid @RequestBody NoteSaveRequest req
	) {
		return ok("Notes saved", notes.save(me(), roomId, req.content(), req.baseVersion()));
	}

	// ---------------- challenges ----------------

	@GetMapping("/challenges")
	public ResponseEntity<ApiResponse<List<ChallengeView>>> challenges(@PathVariable Integer roomId) {
		return ok("Challenges", challenges.list(me(), roomId));
	}

	@PostMapping("/challenges")
	public ResponseEntity<ApiResponse<ChallengeView>> createChallenge(
		@PathVariable Integer roomId,
		@Valid @RequestBody ChallengeRequest req
	) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(new ApiResponse<>(true, "Challenge created", challenges.create(me(), roomId, req)));
	}

	@PostMapping("/challenges/{challengeId}/close")
	public ResponseEntity<ApiResponse<String>> closeChallenge(
		@PathVariable Integer roomId,
		@PathVariable Integer challengeId
	) {
		challenges.close(me(), roomId, challengeId);
		return ok("Challenge closed", "OK");
	}

	@PostMapping("/challenges/{challengeId}/submit")
	public ResponseEntity<ApiResponse<SubmissionView>> submit(
		@PathVariable Integer roomId,
		@PathVariable Integer challengeId,
		@Valid @RequestBody(required = false) SubmitRequest req
	) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(
				new ApiResponse<>(
					true,
					"Submitted",
					challenges.submit(me(), roomId, challengeId, req == null ? null : req.evidence())
				)
			);
	}

	@GetMapping("/challenges/{challengeId}/submissions")
	public ResponseEntity<ApiResponse<List<SubmissionView>>> submissions(
		@PathVariable Integer roomId,
		@PathVariable Integer challengeId
	) {
		return ok("Submissions", challenges.submissionsFor(me(), roomId, challengeId));
	}

	@PostMapping("/challenges/{challengeId}/submissions/{submissionId}/approve")
	public ResponseEntity<ApiResponse<SubmissionView>> approve(
		@PathVariable Integer roomId,
		@PathVariable Integer challengeId,
		@PathVariable Integer submissionId
	) {
		return ok("Approved", challenges.review(me(), roomId, challengeId, submissionId, true));
	}

	@PostMapping("/challenges/{challengeId}/submissions/{submissionId}/reject")
	public ResponseEntity<ApiResponse<SubmissionView>> reject(
		@PathVariable Integer roomId,
		@PathVariable Integer challengeId,
		@PathVariable Integer submissionId
	) {
		return ok("Rejected", challenges.review(me(), roomId, challengeId, submissionId, false));
	}

	// ---------------- attendance ----------------

	@GetMapping("/attendance/me")
	public ResponseEntity<ApiResponse<AttendanceSummary>> myAttendance(@PathVariable Integer roomId) {
		rooms.requireActiveMember(roomId, me());
		return ok("My attendance", attendance.mine(roomId, me()));
	}
}
