package com.skillSwap.Repository;

import com.skillSwap.Entity.StudyRoomEnums.SubmissionStatus;
import com.skillSwap.Entity.StudyRoomSubmission;
import com.skillSwap.Entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomSubmissionRepository extends JpaRepository<StudyRoomSubmission, Integer> {
	Optional<StudyRoomSubmission> findByChallenge_IdAndUser_Id(Integer challengeId, Integer userId);

	@Query(
		"select s from StudyRoomSubmission s join fetch s.user where s.challenge.id = :cid order by s.submittedAt asc"
	)
	List<StudyRoomSubmission> findAllForChallenge(@Param("cid") Integer cid);

	long countByChallenge_IdAndStatus(Integer challengeId, SubmissionStatus status);

	Optional<StudyRoomSubmission> findByIdAndChallenge_Id(Integer id, Integer challengeId);

	/** Moves one status to another exactly once. Returns 0 if somebody already changed it. */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		"update StudyRoomSubmission s set s.status = :to, s.reviewedBy = :by, s.reviewedAt = :now " +
		"where s.id = :id and s.status = :from"
	)
	int reviewFrom(
		@Param("id") Integer id,
		@Param("from") SubmissionStatus from,
		@Param("to") SubmissionStatus to,
		@Param("by") User by,
		@Param("now") Instant now
	);

	/** PENDING -> APPROVED/REJECTED, exactly once. */
	default int review(Integer id, SubmissionStatus to, User by, Instant now) {
		return reviewFrom(id, SubmissionStatus.PENDING, to, by, now);
	}

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update StudyRoomSubmission s set s.rewardPaid = :paid where s.id = :id")
	int setRewardPaid(@Param("id") Integer id, @Param("paid") int paid);
}
