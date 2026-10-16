package com.skillSwap.Entity;

import com.skillSwap.Entity.StudyRoomEnums.*;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
	name = "study_room",
	indexes = {
		@Index(name = "idx_study_room_status", columnList = "status"),
		@Index(name = "idx_study_room_host", columnList = "host_id")
	}
)
@Getter
@Setter
@NoArgsConstructor
public class StudyRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 80)
	private String name;

	@Column(nullable = false, length = 60)
	private String topic;

	@Column(length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private Difficulty difficulty;

	@Column(nullable = false)
	private Integer maxParticipants;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private Visibility visibility;

	/** Only set for PRIVATE rooms. Only ever shown to the host. */
	@Column(length = 12)
	private String accessCode;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "host_id")
	private User host;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private RoomStatus status = RoomStatus.OPEN;

	private Instant scheduledStart;

	private Integer durationMinutes;

	@Column(nullable = false)
	private boolean membersCanPause = false;

	/** Goes up by one every time the host starts a session. Attendance is per session. */
	@Column(nullable = false)
	private Integer sessionNo = 0;

	private Instant sessionStartedAt;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
