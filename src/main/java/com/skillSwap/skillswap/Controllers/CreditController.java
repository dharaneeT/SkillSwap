package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Service.CreditService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/credits")
@Tag(name = "Credit API", description = "Credit management APIs")
public class CreditController {

	private final CreditService creditService;

	public CreditController(CreditService creditService) {
		this.creditService = creditService;
	}

	// ADD CREDITS
	@PostMapping("/{userId}")
	public ResponseEntity<ApiResponse<String>> addCredits(
		@PathVariable Integer userId,
		@Valid @RequestParam Integer credits
	) {
		creditService.addCredits(userId, credits);

		return ResponseEntity.ok(new ApiResponse<>(true, "Credits added", "Success"));
	}
}
