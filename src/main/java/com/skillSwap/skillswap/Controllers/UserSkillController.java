package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.userskill.UserSkillRequestDTO;
import com.skillSwap.skillswap.Dto.userskill.UserSkillResponseDTO;
import com.skillSwap.skillswap.Service.UserSkillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/user-skill")
@Tag(name = "User_Skill API", description = "User_Skill management APIs")
public class UserSkillController {

	@Autowired
	private UserSkillService userSkillService;

	public UserSkillController(UserSkillService userSkillService) {
		this.userSkillService = userSkillService;
	}

	@PostMapping
	public ResponseEntity<ApiResponse<UserSkillResponseDTO>> addUserSkill(@Valid @RequestBody UserSkillRequestDTO dto) {
		UserSkillResponseDTO data = userSkillService.addUserSkill(dto);

		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "User Skill added", data));
	}

	@GetMapping("/get")
	public ResponseEntity<ApiResponse<List<UserSkillResponseDTO>>> getUserSkill() {
		return ResponseEntity.ok(new ApiResponse<>(true, "All user Skills", userSkillService.getUserSkill()));
	}

	//GET USER_SKILL BY ID
	@GetMapping("get/{id}")
	public ResponseEntity<ApiResponse<UserSkillResponseDTO>> getUsById(@Valid @PathVariable Integer id) {
		return ResponseEntity.ok(new ApiResponse<>(true, "User_Skill Returned", userSkillService.getUserSkillById(id)));
	}
}
