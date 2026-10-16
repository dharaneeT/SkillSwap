package com.skillSwap.Entity;

import com.skillSwap.Entity.StudyRoomEnums.*;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "study_room_challenge", indexes = @Index(name = "idx_study_challenge_room", columnList = "room_id"))
@Getter
@Setter
@NoArgsConstructor
public class StudyRoomChallenge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id")
	private StudyRoom room;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 500)
	private String description;

	@Column(nullable = false, length = 300)
	private String completionCriteria;

	private Instant deadline;

	/** The most credits one approved submission can earn (0-3). The daily cap can still lower the payout. */
	@Column(nullable = false)
	private int rewardCredits = 0;

	@Column(nullable = false)
	private boolean evidenceRequired = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private ChallengeStatus status = ChallengeStatus.OPEN;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();
}
