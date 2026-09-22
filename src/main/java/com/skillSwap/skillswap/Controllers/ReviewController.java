package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.review.ReviewRequestDTO;
import com.skillSwap.skillswap.Dto.review.ReviewResponseDTO;
import com.skillSwap.skillswap.Entity.Review;
import com.skillSwap.skillswap.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/review")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping("/add")
	public ApiResponse<ReviewResponseDTO> addReview(@RequestBody ReviewRequestDTO dto) {
		return new ApiResponse<>(true, "Review Added", reviewService.addReview(dto));
	}
}
