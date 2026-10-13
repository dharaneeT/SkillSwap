package com.skillSwap.Controllers;

import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.skill.SkillModerationDTO;
import com.skillSwap.Service.SkillModerationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/admin/skills")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Skill Moderation", description = "Approve and merge skills, ROLE_ADMIN only")
@SecurityRequirement(name = "bearerAuth")
public class AdminSkillController {

	public record MergeRequest(@NotNull Integer targetId) {}

	private final SkillModerationService service;

	public AdminSkillController(SkillModerationService service) {
		this.service = service;
	}

	@GetMapping("/pending")
	public ResponseEntity<ApiResponse<List<SkillModerationDTO>>> pending() {
		return ResponseEntity.ok(new ApiResponse<>(true, "Pending skills", service.listPending()));
	}

	@PutMapping("/{id}/approve")
	public ResponseEntity<ApiResponse<String>> approve(@PathVariable Integer id) {
		service.approve(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "Skill approved", "OK"));
	}

	@PostMapping("/{id}/merge")
	public ResponseEntity<ApiResponse<String>> merge(@PathVariable Integer id, @Valid @RequestBody MergeRequest body) {
		service.merge(id, body.targetId());
		return ResponseEntity.ok(new ApiResponse<>(true, "Skills merged", "OK"));
	}
}
