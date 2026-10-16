package com.skillSwap.Entity;

import com.skillSwap.Entity.StudyRoomEnums.*;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One row per (challenge, user): the unique constraint is what stops duplicate completions and duplicate rewards. */
@Entity
@Table(
	name = "study_room_submission",
	uniqueConstraints = @UniqueConstraint(name = "uk_study_submission", columnNames = { "challenge_id", "user_id" })
)
@Getter
@Setter
@NoArgsConstructor
public class StudyRoomSubmission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "challenge_id")
	private StudyRoomChallenge challenge;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(length = 1000)
	private String evidence;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
	private SubmissionStatus status = SubmissionStatus.PENDING;

	@Column(nullable = false)
	private Instant submittedAt = Instant.now();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewed_by")
	private User reviewedBy;

	private Instant reviewedAt;

	/** Credits actually paid out (can be lower than the challenge reward because of the daily cap). */
	@Column(nullable = false)
	private int rewardPaid = 0;
}
