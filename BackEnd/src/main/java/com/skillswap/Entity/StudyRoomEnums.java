package com.skillSwap.Entity;

public final class StudyRoomEnums {

	private StudyRoomEnums() {}

	public enum Difficulty {
		BEGINNER,
		INTERMEDIATE,
		ADVANCED
	}

	public enum Visibility {
		PUBLIC,
		PRIVATE
	}

	/** OPEN = room exists, no session running. LIVE = host started a study session. */
	public enum RoomStatus {
		OPEN,
		LIVE,
		ARCHIVED
	}

	public enum MemberRole {
		HOST,
		MEMBER
	}

	/** REMOVED = the host kicked this person; they cannot come back until restored. */
	public enum MemberStatus {
		ACTIVE,
		REMOVED
	}

	public enum TimerPhase {
		FOCUS,
		SHORT_BREAK,
		LONG_BREAK
	}

	public enum TimerStatus {
		IDLE,
		RUNNING,
		PAUSED,
		FINISHED
	}

	public enum ChallengeStatus {
		OPEN,
		CLOSED
	}

	public enum SubmissionStatus {
		PENDING,
		APPROVED,
		REJECTED
	}
}
