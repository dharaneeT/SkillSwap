package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Entity.Review;
import com.skillSwap.skillswap.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/skillswap/v1/review")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping("/add")
	public Review addReview(@RequestParam int sessionId, @RequestParam int ratings, @RequestParam String comment) {
		return reviewService.addReview(sessionId, ratings, comment);
	}
}
