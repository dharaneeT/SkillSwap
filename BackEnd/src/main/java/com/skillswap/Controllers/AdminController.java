package com.skillSwap.Controllers;

import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.user.UserResponseDTO;
import com.skillSwap.Entity.Role;
import com.skillSwap.Service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin API", description = "Moderation — ROLE_ADMIN only")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/users")
	public ResponseEntity<ApiResponse<List<UserResponseDTO>>> users() {
		return ResponseEntity.ok(new ApiResponse<>(true, "All users", adminService.listUsers()));
	}

	@PutMapping("/users/{id}/deactivate")
	public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable Integer id) {
		adminService.setActive(id, false);
		return ResponseEntity.ok(new ApiResponse<>(true, "User deactivated", "OK"));
	}

	@PutMapping("/users/{id}/activate")
	public ResponseEntity<ApiResponse<String>> activate(@PathVariable Integer id) {
		adminService.setActive(id, true);
		return ResponseEntity.ok(new ApiResponse<>(true, "User activated", "OK"));
	}

	@PutMapping("/users/{id}/role")
	public ResponseEntity<ApiResponse<String>> role(@PathVariable Integer id, @RequestParam Role role) {
		adminService.setRole(id, role);
		return ResponseEntity.ok(new ApiResponse<>(true, "Role updated", "OK"));
	}

	@DeleteMapping("/skills/{id}")
	public ResponseEntity<ApiResponse<String>> deleteSkill(@PathVariable Integer id) {
		adminService.deleteSkill(id);
		return ResponseEntity.ok(new ApiResponse<>(true, "Skill deleted", "OK"));
	}
}
