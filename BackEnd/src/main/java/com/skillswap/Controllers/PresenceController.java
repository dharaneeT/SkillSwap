package com.skillSwap.Controllers;

import com.skillSwap.Dto.presence.PresenceDTO;
import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Service.PresenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/presence")
@Tag(name = "Presence API", description = "Online status and last seen")
public class PresenceController {

	private final PresenceService presenceService;

	public PresenceController(PresenceService presenceService) {
		this.presenceService = presenceService;
	}

	// POST /skillswap/v1/presence/heartbeat
	@PostMapping("/heartbeat")
	public ResponseEntity<ApiResponse<String>> heartbeat() {
		presenceService.heartbeat();
		return ResponseEntity.ok(new ApiResponse<>(true, "OK", "OK"));
	}

	// GET /skillswap/v1/presence?ids=1,2,3   (max 50 ids)
	@GetMapping
	public ResponseEntity<ApiResponse<List<PresenceDTO>>> presence(@RequestParam List<Integer> ids) {
		return ResponseEntity.ok(new ApiResponse<>(true, "Presence", presenceService.getPresence(ids)));
	}
}
