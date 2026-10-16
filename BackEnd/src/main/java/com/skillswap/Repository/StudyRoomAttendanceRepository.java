package com.skillSwap.Repository;

import com.skillSwap.Entity.StudyRoomAttendance;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomAttendanceRepository extends JpaRepository<StudyRoomAttendance, Integer> {
	Optional<StudyRoomAttendance> findByRoom_IdAndUser_IdAndSessionNo(
		Integer roomId,
		Integer userId,
		Integer sessionNo
	);

	List<StudyRoomAttendance> findByRoom_IdAndUser_IdOrderBySessionNoDesc(
		Integer roomId,
		Integer userId,
		Pageable pageable
	);

	/** Adds time only if lastActiveAt is still what we read, so two browser tabs can never count the same seconds twice. */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		"update StudyRoomAttendance a set a.activeSeconds = a.activeSeconds + :d, a.lastActiveAt = :now " +
		"where a.id = :id and a.lastActiveAt = :prev"
	)
	int addActive(@Param("id") Integer id, @Param("d") int d, @Param("now") Instant now, @Param("prev") Instant prev);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update StudyRoomAttendance a set a.lastActiveAt = :now where a.id = :id")
	int touch(@Param("id") Integer id, @Param("now") Instant now);

	/** The one-time gate for the attendance reward: returns 1 for exactly one caller, ever. */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		"update StudyRoomAttendance a set a.rewardIssued = true, a.completed = true where a.id = :id and a.rewardIssued = false"
	)
	int claimReward(@Param("id") Integer id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update StudyRoomAttendance a set a.rewardCredits = :c where a.id = :id")
	int setRewardCredits(@Param("id") Integer id, @Param("c") int credits);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		"update StudyRoomAttendance a set a.lastLeftAt = :now where a.room.id = :r and a.user.id = :u and a.sessionNo = :s"
	)
	int markLeft(
		@Param("r") Integer roomId,
		@Param("u") Integer userId,
		@Param("s") Integer sessionNo,
		@Param("now") Instant now
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		"update StudyRoomAttendance a set a.focusCycles = a.focusCycles + 1 where a.room.id = :r and a.sessionNo = :s and a.user.id in :ids"
	)
	int incrementFocus(
		@Param("r") Integer roomId,
		@Param("s") Integer sessionNo,
		@Param("ids") Collection<Integer> userIds
	);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
		"update StudyRoomAttendance a set a.completed = true where a.room.id = :r and a.sessionNo = :s and a.activeSeconds >= :req"
	)
	int completeQualified(@Param("r") Integer roomId, @Param("s") Integer sessionNo, @Param("req") int requiredSeconds);
}
