package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Entity.Review;
import com.skillSwap.skillswap.Entity.Session;
import com.skillSwap.skillswap.Repository.ReviewRepository;
import com.skillSwap.skillswap.Repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private SessionRepository sessionRepository;

	public Review addReview(int sessionId, int ratings, String comment) {
		Session session = sessionRepository
			.findById(sessionId)
			.orElseThrow(() -> new RuntimeException("Session Not Found"));

		Review review = new Review();
		review.setComment(comment);
		review.setSession(session);
		review.setRating(ratings);
		return reviewRepository.save(review);
	}
}
