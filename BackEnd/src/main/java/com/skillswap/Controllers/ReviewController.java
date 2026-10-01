package com.skillSwap.Controllers;

import com.skillSwap.Dto.common.PageResponse;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.review.ReviewRequestDTO;
import com.skillSwap.Dto.review.ReviewResponseDTO;
import com.skillSwap.Service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/review")
@Tag(name = "Review API", description = "Review management APIs")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping("/add")
	public ResponseEntity<ApiResponse<ReviewResponseDTO>> addReview(@Valid @RequestBody ReviewRequestDTO dto) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(new ApiResponse<>(true, "Review added", reviewService.addReview(dto)));
	}

	// reviews RECEIVED by a provider, newest first
	@GetMapping("/user/{userId}")
	public ResponseEntity<ApiResponse<PageResponse<ReviewResponseDTO>>> forUser(
		@PathVariable Integer userId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		if (page < 0 || size < 1 || size > 50) throw new IllegalArgumentException(
			"page must be >= 0 and size between 1 and 50"
		);
		return ResponseEntity.ok(new ApiResponse<>(true, "Reviews", reviewService.reviewsForUser(userId, page, size)));
	}
}
