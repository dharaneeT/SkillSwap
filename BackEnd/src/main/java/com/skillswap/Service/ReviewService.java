package com.skillSwap.Service;

import com.skillSwap.Dto.common.PageResponse;
import com.skillSwap.Dto.review.ReviewRequestDTO;
import com.skillSwap.Dto.review.ReviewResponseDTO;
import com.skillSwap.Entity.*;
import com.skillSwap.Exception.DuplicateResourceException;
import com.skillSwap.Exception.InvalidSessionStateException;
import com.skillSwap.Exception.SessionNotFoundException;
import com.skillSwap.Exception.UserNotFoundException;
import com.skillSwap.Repository.ReviewRepository;
import com.skillSwap.Repository.SessionRepository;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.Security.CurrentUserService;
import java.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final SessionRepository sessionRepository;
	private final UserRepository userRepository;
	private final CurrentUserService currentUserService;

	public ReviewService(
		ReviewRepository reviewRepository,
		SessionRepository sessionRepository,
		UserRepository userRepository,
		CurrentUserService currentUserService
	) {
		this.reviewRepository = reviewRepository;
		this.sessionRepository = sessionRepository;
		this.userRepository = userRepository;
		this.currentUserService = currentUserService;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	@CacheEvict(cacheNames = "skillSearch", allEntries = true)
	public ReviewResponseDTO addReview(ReviewRequestDTO dto) {
		User me = currentUserService.getCurrentUser();

		// row lock: two simultaneous submits for one session can't both pass the checks below
		Session session = sessionRepository
			.findByIdForUpdate(dto.getSessionId())
			.orElseThrow(() -> new SessionNotFoundException(dto.getSessionId()));

		if (!session.getLearner().getId().equals(me.getId())) throw new AccessDeniedException(
			"Only the learner of this session can review it"
		);
		if (session.getStatus() != SessionStatus.COMPLETED) throw new InvalidSessionStateException(
			"You can only review a completed session"
		);
		if (reviewRepository.existsBySession_Id(session.getId())) throw new DuplicateResourceException(
			"This session has already been reviewed"
		);

		Review review = new Review();
		review.setSession(session);
		review.setRating(dto.getRating());
		review.setComment(dto.getComment() == null ? null : dto.getComment().trim());
		review.setCreatedAt(LocalDateTime.now());
		Review saved = reviewRepository.save(review);

		refreshProviderRating(session.getProvider().getId());
		return toDto(saved);
	}

	@Transactional(readOnly = true)
	public PageResponse<ReviewResponseDTO> reviewsForUser(Integer userId, int page, int size) {
		if (!userRepository.existsById(userId)) throw new UserNotFoundException(userId);
		return PageResponse.of(
			reviewRepository
				.findBySession_Provider_Id(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
				.map(this::toDto)
		);
	}

	// recompute from the source of truth; the provider row lock serializes concurrent updates
	private void refreshProviderRating(Integer providerId) {
		userRepository.findByIdForUpdate(providerId);
		ReviewRepository.RatingStats stats = reviewRepository.statsForProvider(providerId);
		double avg = stats.getAverage() == null ? 0.0 : Math.round(stats.getAverage() * 100.0) / 100.0;
		int total = stats.getTotal() == null ? 0 : stats.getTotal().intValue();
		userRepository.updateRating(providerId, avg, total);
	}

	private ReviewResponseDTO toDto(Review r) {
		return new ReviewResponseDTO(
			r.getId(),
			r.getSession().getId(),
			r.getSession().getLearner().getName(),
			r.getRating(),
			r.getComment(),
			r.getCreatedAt()
		);
	}
}
