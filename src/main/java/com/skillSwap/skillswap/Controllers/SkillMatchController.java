package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.Match.MatchResponseDTO;
import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Service.MatchService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/skillswap/v1/match")
public class SkillMatchController {

	@Autowired
	private MatchService matchService;

	public SkillMatchController(MatchService matchService) {
		this.matchService = matchService;
	}

	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<List<MatchResponseDTO>>> getMatch(@PathVariable Integer userId) {
		return ResponseEntity.ok(new ApiResponse<>(true, "Matched Skills Returned", matchService.findMatches(userId)));
	}
}
