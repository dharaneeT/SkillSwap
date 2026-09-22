package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.userskill.UserSkillRequestDTO;
import com.skillSwap.skillswap.Dto.userskill.UserSkillResponseDTO;
import com.skillSwap.skillswap.Service.UserSkillService;
import org.springframework.beans.factory.annotation.Autowired;
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
	public ApiResponse<UserSkillResponseDTO> addUserSkill(@RequestBody UserSkillRequestDTO dto) {
		return new ApiResponse<>(true, "User Skill is Added", userSkillService.addUserSkill(dto));
	}
}
