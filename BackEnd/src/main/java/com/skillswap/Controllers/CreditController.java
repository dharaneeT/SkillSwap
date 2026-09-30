package com.skillSwap.Controllers;

import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Service.CreditService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{userId}")
	public ResponseEntity<ApiResponse<String>> addCredits(
		@PathVariable Integer userId,
		@Valid @RequestParam Integer credits
	) {

		creditService.addCredits(userId, credits);

		return ResponseEntity.ok(new ApiResponse<>(true, "Credits added", "Success"));
	}
}
