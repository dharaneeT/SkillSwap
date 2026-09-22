package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Service.CreditService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/credits")
public class CreditController {

	private final CreditService creditService;

	public CreditController(CreditService creditService) {
		this.creditService = creditService;
	}

	// ADD CREDITS
	@PostMapping("/{userId}")
	public ApiResponse<String> addCredits(@PathVariable Integer userId, @RequestParam Integer credits) {
		creditService.addCredits(userId, credits);

		return new ApiResponse<>(true, "Credits added", "Done");
	}
}
