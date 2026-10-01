package com.skillSwap.Controllers;

import com.skillSwap.Dto.credit.CreditResponseDTO;
import com.skillSwap.Dto.credit.CreditTransactionDTO;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Entity.CreditTxType;
import com.skillSwap.Entity.User;
import com.skillSwap.Security.CurrentUserService;
import com.skillSwap.Service.CreditWalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/credits")
@Tag(name = "Credit API", description = "Credit wallet APIs")
public class CreditController {

	private final CreditWalletService wallet;
	private final CurrentUserService currentUserService;

	public CreditController(CreditWalletService wallet, CurrentUserService currentUserService) {
		this.wallet = wallet;
		this.currentUserService = currentUserService;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{userId}")
	public ResponseEntity<ApiResponse<String>> addCredits(@PathVariable Integer userId, @RequestParam Integer credits) {
		wallet.credit(userId, credits, CreditTxType.ADMIN_GRANT, null, "Granted by admin");
		return ResponseEntity.ok(new ApiResponse<>(true, "Credits added", "Success"));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<CreditResponseDTO>> myBalance() {
		User me = currentUserService.getCurrentUser();
		return ResponseEntity.ok(
			new ApiResponse<>(
				true,
				"Balance",
				new CreditResponseDTO(me.getId(), me.getName(), wallet.getBalance(me.getId()))
			)
		);
	}

	@GetMapping("/me/transactions")
	public ResponseEntity<ApiResponse<List<CreditTransactionDTO>>> myTransactions() {
		User me = currentUserService.getCurrentUser();
		return ResponseEntity.ok(new ApiResponse<>(true, "Transactions", wallet.history(me.getId())));
	}
}
