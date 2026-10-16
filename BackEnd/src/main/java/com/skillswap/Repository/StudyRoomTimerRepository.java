package com.skillSwap.Repository;

import com.skillSwap.Entity.StudyRoomEnums.TimerStatus;
import com.skillSwap.Entity.StudyRoomTimer;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomTimerRepository extends JpaRepository<StudyRoomTimer, Integer> {
	Optional<StudyRoomTimer> findByRoom_Id(Integer roomId);

	List<StudyRoomTimer> findByRoom_IdIn(Collection<Integer> roomIds);

	/** Row lock: start/pause/resume/reset/expiry can never interleave on the same room. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from StudyRoomTimer t where t.room.id = :roomId")
	Optional<StudyRoomTimer> findByRoomIdForUpdate(@Param("roomId") Integer roomId);

	@Query("select t.room.id from StudyRoomTimer t where t.status = :running and t.endsAt <= :now")
	List<Integer> findDueRoomIds(@Param("running") TimerStatus running, @Param("now") Instant now);
}
