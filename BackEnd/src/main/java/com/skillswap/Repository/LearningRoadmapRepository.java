package com.skillSwap.Repository;

import com.skillSwap.Entity.LearningRoadmap;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningRoadmapRepository extends JpaRepository<LearningRoadmap, Long> {
	List<LearningRoadmap> findByOwner_IdOrderByUpdatedAtDesc(Integer ownerId);

	// ownership is part of the query: someone else's roadmap simply "does not exist" for you
	Optional<LearningRoadmap> findByIdAndOwner_Id(Long id, Integer ownerId);

	long countByOwner_Id(Integer ownerId);
}
