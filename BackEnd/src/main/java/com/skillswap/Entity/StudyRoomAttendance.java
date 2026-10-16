package com.skillSwap.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One row per (room, user, session). History only: "who is online now" is NOT read from here. */
@Entity
@Table(
	name = "study_room_attendance",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_study_attendance",
		columnNames = { "room_id", "user_id", "session_no" }
	),
	indexes = @Index(name = "idx_study_attendance_user", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
public class StudyRoomAttendance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id")
	private StudyRoom room;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "session_no", nullable = false)
	private Integer sessionNo;

	@Column(nullable = false)
	private Instant firstJoinedAt = Instant.now();

	private Instant lastLeftAt;

	@Column(name = "last_active_at")
	private Instant lastActiveAt;

	/** Seconds the server saw this user connected, tab visible, with at least one other person present. */
	@Column(nullable = false)
	private int activeSeconds = 0;

	@Column(nullable = false)
	private int focusCycles = 0;

	@Column(nullable = false)
	private boolean completed = false;

	@Column(nullable = false)
	private boolean rewardIssued = false;

	@Column(nullable = false)
	private int rewardCredits = 0;
}
