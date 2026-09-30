package com.skillSwap.Service;

import com.skillSwap.Dto.review.ReviewRequestDTO;
import com.skillSwap.Dto.review.ReviewResponseDTO;
import com.skillSwap.Entity.Review;
import com.skillSwap.Entity.Session;
import com.skillSwap.Repository.ReviewRepository;
import com.skillSwap.Repository.SessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

	//	@Autowired
	private ReviewRepository reviewRepository;

	//	@Autowired
	private SessionRepository sessionRepository;

	//	@Autowired
	private ModelMapper modelMapper;

	public ReviewService(
		ReviewRepository reviewRepository,
		ModelMapper modelMapper,
		SessionRepository sessionRepository
	) {
		this.reviewRepository = reviewRepository;
		this.modelMapper = modelMapper;
		this.sessionRepository = sessionRepository;
	}

	//Add Review
	@Operation(summary = "ADD A NEW REVIEW")
	public ReviewResponseDTO addReview(ReviewRequestDTO dto) {
		Session session = sessionRepository
			.findById(dto.getSessionId())
			.orElseThrow(() -> new RuntimeException("Session Not Found"));

		Review review = new Review();
		review.setComment(dto.getComment());
		review.setRating(dto.getRating());
		review.setSession(session);

		Review saved = reviewRepository.save(review);

		ReviewResponseDTO responseDTO = new ReviewResponseDTO();
		responseDTO.setComment(saved.getComment());
		responseDTO.setRating(saved.getRating());
		responseDTO.setId(saved.getId());
		responseDTO.setName(session.getLearner().getName());

		return responseDTO;
	}
}
