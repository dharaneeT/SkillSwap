package com.skillSwap.Repository;

import com.skillSwap.Entity.StudyRoomEnums.MemberStatus;
import com.skillSwap.Entity.StudyRoomMember;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRoomMemberRepository extends JpaRepository<StudyRoomMember, Integer> {
	Optional<StudyRoomMember> findByRoom_IdAndUser_Id(Integer roomId, Integer userId);

	@Query("select m from StudyRoomMember m join fetch m.user where m.room.id = :roomId order by m.joinedAt asc")
	List<StudyRoomMember> findAllForRoom(@Param("roomId") Integer roomId);

	@Query("select m.room.id from StudyRoomMember m where m.user.id = :uid and m.status = :status")
	Set<Integer> findRoomIdsByUser(@Param("uid") Integer uid, @Param("status") MemberStatus status);
}
