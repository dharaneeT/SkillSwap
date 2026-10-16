package com.skillSwap.Repository;

import com.skillSwap.Entity.StudyRoom;
import com.skillSwap.Entity.StudyRoomEnums.MemberStatus;
import com.skillSwap.Entity.StudyRoomEnums.RoomStatus;
import com.skillSwap.Entity.StudyRoomEnums.Visibility;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Integer> {
	/** Public rooms + private rooms where this user is an ACTIVE member. Private rooms of others are never returned. */
	@Query(
		"select r from StudyRoom r join fetch r.host where r.status <> :archived and " +
		"(r.visibility = :pub or r.id in (select m.room.id from StudyRoomMember m where m.user.id = :uid and m.status = :active)) " +
		"order by r.createdAt desc"
	)
	List<StudyRoom> findVisibleFor(
		@Param("uid") Integer uid,
		@Param("archived") RoomStatus archived,
		@Param("pub") Visibility pub,
		@Param("active") MemberStatus active,
		Pageable pageable
	);

	long countByHost_IdAndStatusNot(Integer hostId, RoomStatus status);

	List<StudyRoom> findByStatus(RoomStatus status);
}
