package com.skillSwap.Repository;

import com.skillSwap.Entity.Review;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
	boolean existsBySession_Id(Integer sessionId);

	Page<Review> findBySession_Provider_Id(Integer providerId, Pageable pageable);

	interface RatingStats {
		Double getAverage();
		Long getTotal();
	}

	// reviews WRITTEN by a learner
	Page<Review> findBySession_Learner_Id(Integer learnerId, Pageable pageable);

	@Query("SELECT AVG(r.rating) AS average, COUNT(r) AS total FROM Review r WHERE r.session.provider.id = :providerId")
	RatingStats statsForProvider(@Param("providerId") Integer providerId);

	@Query("SELECT r.session.id FROM Review r WHERE r.session.id IN :ids")
	List<Integer> findReviewedSessionIds(@Param("ids") Collection<Integer> ids);
}
