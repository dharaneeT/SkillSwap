package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.review.ReviewRequestDTO;
import com.skillSwap.skillswap.Dto.review.ReviewResponseDTO;
import com.skillSwap.skillswap.Entity.Review;
import com.skillSwap.skillswap.Service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/review")
@Tag(name = "Review API", description = "Review management APIs")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping("/add")
	public ResponseEntity<ApiResponse<ReviewResponseDTO>> addReview(@Valid @RequestBody ReviewRequestDTO dto) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(new ApiResponse<>(true, "Review added", reviewService.addReview(dto)));
	}
}
