package com.skillSwap.Repository;

import com.skillSwap.Entity.StudyRoomChallenge;
import com.skillSwap.Entity.StudyRoomEnums.ChallengeStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRoomChallengeRepository extends JpaRepository<StudyRoomChallenge, Integer> {
	List<StudyRoomChallenge> findByRoom_IdOrderByCreatedAtDesc(Integer roomId, Pageable pageable);

	Optional<StudyRoomChallenge> findByIdAndRoom_Id(Integer id, Integer roomId);

	long countByRoom_IdAndStatus(Integer roomId, ChallengeStatus status);
}
