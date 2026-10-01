package com.skillSwap.Repository;

import com.skillSwap.Entity.Session;
import com.skillSwap.Entity.SessionStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
	List<Session> findByStatus(SessionStatus status);

	@Query("SELECT s FROM Session s WHERE s.provider.id = :userId OR s.learner.id = :userId")
	List<Session> findUserSessions(int userId);


	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Session s WHERE s.id = :id")
	Optional<Session> findByIdForUpdate(@Param("id") Integer id);

	// how many sessions with this status overlap the window (excluding one id)
	@Query(
		"""
    SELECT COUNT(s) FROM Session s
    WHERE (s.provider.id = :userId OR s.learner.id = :userId)
      AND s.status = :status
      AND s.sessionTime > :from AND s.sessionTime < :to
      AND s.id <> :excludeId
"""
	)
	long countOverlaps(
		@Param("userId") Integer userId,
		@Param("status") SessionStatus status,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to,
		@Param("excludeId") int excludeId
	);

	boolean existsByProvider_IdAndLearner_IdAndSessionTimeAndStatus(
		Integer providerId,
		Integer learnerId,
		LocalDateTime time,
		SessionStatus status
	);

	@Query(
		"""
    SELECT s FROM Session s
    WHERE s.status = :status AND s.reminderSent = false
      AND s.sessionTime > :now AND s.sessionTime <= :until
"""
	)
	List<Session> findDueForReminder(
		@Param("status") SessionStatus status,
		@Param("now") LocalDateTime now,
		@Param("until") LocalDateTime until
	);
}
