package com.skillSwap.Controllers;

import com.skillSwap.Dto.leaderboard.LeaderboardEntryDTO;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Service.LeaderboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/leaderboard")
@Tag(name = "Leaderboard API", description = "Top teachers and personal standing")
public class LeaderboardController {

	private final LeaderboardService leaderboardService;

	public LeaderboardController(LeaderboardService leaderboardService) {
		this.leaderboardService = leaderboardService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<LeaderboardEntryDTO>>> top(@RequestParam(defaultValue = "20") int limit) {
		return ResponseEntity.ok(new ApiResponse<>(true, "Leaderboard", leaderboardService.getLeaderboard(limit)));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<LeaderboardEntryDTO>> mine() {
		return ResponseEntity.ok(new ApiResponse<>(true, "My standing", leaderboardService.getMyStanding()));
	}
}
