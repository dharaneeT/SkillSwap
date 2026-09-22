package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.userskill.UserSkillRequestDTO;
import com.skillSwap.skillswap.Dto.userskill.UserSkillResponseDTO;
import com.skillSwap.skillswap.Service.UserSkillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/user-skill")
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
}
