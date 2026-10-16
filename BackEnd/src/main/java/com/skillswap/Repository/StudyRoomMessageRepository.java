package com.skillSwap.Repository;

import com.skillSwap.Entity.StudyRoomMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomMessageRepository extends JpaRepository<StudyRoomMessage, Long> {
	/** Newest first. Pass Long.MAX_VALUE as "before" for the latest page. */
	@Query(
		"select m from StudyRoomMessage m join fetch m.sender " +
		"where m.room.id = :roomId and m.deleted = false and m.id < :before order by m.id desc"
	)
	List<StudyRoomMessage> findPage(@Param("roomId") Integer roomId, @Param("before") Long before, Pageable pageable);

	Optional<StudyRoomMessage> findByRoom_IdAndSender_IdAndClientId(Integer roomId, Integer senderId, String clientId);

	Optional<StudyRoomMessage> findByIdAndRoom_Id(Long id, Integer roomId);
}
