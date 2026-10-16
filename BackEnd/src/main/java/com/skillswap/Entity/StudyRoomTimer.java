package com.skillSwap.Entity;

import com.skillSwap.Entity.StudyRoomEnums.*;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One row per room. This row (not any browser) is the source of truth for the shared timer. */
@Entity
@Table(name = "study_room_timer", indexes = @Index(name = "idx_study_timer_status_end", columnList = "status, ends_at"))
@Getter
@Setter
@NoArgsConstructor
public class StudyRoomTimer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", unique = true)
	private StudyRoom room;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private TimerPhase phase = TimerPhase.FOCUS;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private TimerStatus status = TimerStatus.IDLE;

	@Column(nullable = false)
	private int durationSeconds = 1500;

	/** The moment the countdown reaches zero (only while RUNNING). Everyone calculates from this. */
	@Column(name = "ends_at")
	private Instant endsAt;

	/** Seconds left while PAUSED (or the full duration while IDLE). */
	@Column(nullable = false)
	private int remainingSeconds = 1500;

	@Column(nullable = false)
	private long revision = 0;

	private Instant updatedAt;
}
