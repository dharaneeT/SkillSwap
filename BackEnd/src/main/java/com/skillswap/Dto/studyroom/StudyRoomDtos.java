package com.skillSwap.Dto.studyroom;

import com.skillSwap.Entity.StudyRoomEnums.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

/** All study-room request/response shapes in one file (same style as RoadmapDtos). */
public final class StudyRoomDtos {

	private StudyRoomDtos() {}

	// ---------- requests ----------

	/** Used for both "create room" and "edit room". */
	public record RoomRequest(
		@NotBlank(message = "Room name is required")
		@Size(min = 3, max = 80, message = "Room name must be 3-80 characters")
		String name,
		@NotBlank(message = "Topic is required") @Size(max = 60, message = "Topic is too long (max 60)") String topic,
		@Size(max = 500, message = "Description is too long (max 500)") String description,
		@NotNull(message = "Difficulty is required") Difficulty difficulty,
		@NotNull(message = "Maximum participants is required")
		@Min(value = 2, message = "A room needs room for at least 2 people")
		@Max(value = 50, message = "A room can hold at most 50 people")
		Integer maxParticipants,
		@NotNull(message = "Visibility is required") Visibility visibility,
		Instant scheduledStart,
		@Min(value = 5, message = "Duration must be at least 5 minutes")
		@Max(value = 480, message = "Duration can be at most 480 minutes")
		Integer durationMinutes,
		Boolean membersCanPause
	) {}

	public record JoinRequest(@Size(max = 12, message = "Invalid access code") String accessCode) {}

	public record TimerStartRequest(
		@NotNull(message = "Phase is required") TimerPhase phase,
		@Min(value = 60, message = "Custom duration must be at least 1 minute")
		@Max(value = 10800, message = "Custom duration can be at most 180 minutes")
		Integer durationSeconds
	) {}

	public record NoteSaveRequest(
		@NotNull(message = "Content is required")
		@Size(max = 20000, message = "Notes are too long (max 20000 characters)")
		String content,
		@NotNull(message = "baseVersion is required") Long baseVersion
	) {}

	public record ChallengeRequest(
		@NotBlank(message = "Title is required") @Size(max = 100, message = "Title is too long (max 100)") String title,
		@Size(max = 500, message = "Description is too long (max 500)") String description,
		@NotBlank(message = "Completion criteria is required")
		@Size(max = 300, message = "Criteria is too long (max 300)")
		String completionCriteria,
		Instant deadline,
		@Min(value = 0, message = "Reward cannot be negative")
		@Max(value = 3, message = "Reward can be at most 3 credits")
		Integer rewardCredits,
		Boolean evidenceRequired
	) {}

	public record SubmitRequest(@Size(max = 1000, message = "Evidence is too long (max 1000)") String evidence) {}

	// ---------- responses ----------

	public record HostRef(Integer id, String name) {}

	public record RoomCard(
		Integer id,
		String name,
		String topic,
		String description,
		Difficulty difficulty,
		Visibility visibility,
		RoomStatus status,
		Integer maxParticipants,
		int participants,
		HostRef host,
		Instant scheduledStart,
		Integer durationMinutes,
		String activity,
		boolean member
	) {}

	public record MeInfo(Integer userId, String name, boolean host) {}

	public record RoomDetail(
		RoomCard room,
		boolean membersCanPause,
		int sessionNo,
		Instant sessionStartedAt,
		String accessCode, // only filled for the host
		MeInfo me,
		TimerView timer,
		int minAttendanceMinutes
	) {}

	/** Pushed to everybody in the room when status/session/details change. */
	public record RoomEvent(RoomStatus status, int sessionNo, Instant sessionStartedAt) {}

	public record PresenceEntry(Integer userId, String name, boolean host) {}

	public record MemberView(Integer userId, String name, MemberRole role, MemberStatus status, Instant joinedAt) {}

	public record ChatMessage(
		Long id,
		String clientId,
		Integer senderId,
		String senderName,
		String content,
		Instant createdAt
	) {}

	public record TimerView(
		TimerPhase phase,
		TimerStatus status,
		int durationSeconds,
		Instant endsAt,
		int remainingSeconds,
		Instant serverNow,
		long revision
	) {}

	public record NoteView(
		String content,
		long version,
		Integer updatedById,
		String updatedByName,
		Instant updatedAt
	) {}

	/** What other people get pushed when notes change (no content: they fetch it when they want it). */
	public record NoteEvent(long version, Integer updatedById, String updatedByName, Instant updatedAt) {}

	public record SubmissionView(
		Integer id,
		Integer userId,
		String userName,
		String evidence,
		SubmissionStatus status,
		Instant submittedAt,
		int rewardPaid
	) {}

	public record ChallengeView(
		Integer id,
		String title,
		String description,
		String completionCriteria,
		Instant deadline,
		int rewardCredits,
		boolean evidenceRequired,
		ChallengeStatus status,
		long approvedCount,
		long pendingCount, // only filled for the host
		SubmissionView mine
	) {}

	public record ChallengeEvent(String event, Integer challengeId) {}

	public record AttendanceView(
		int sessionNo,
		Instant firstJoinedAt,
		Instant lastLeftAt,
		int activeSeconds,
		int requiredSeconds,
		int focusCycles,
		boolean completed,
		boolean rewardIssued,
		int rewardCredits
	) {}

	public record AttendanceSummary(AttendanceView current, List<AttendanceView> history) {}

	public record RewardEvent(int credits, String reason) {}
}
