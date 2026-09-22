package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.skill.SkillRequestDTO;
import com.skillSwap.skillswap.Dto.skill.SkillResponseDTO;
import com.skillSwap.skillswap.Service.SkillService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/skill")
public class SkillController {

	@Autowired
	private SkillService skillService;

	public SkillController(SkillService skillService) {
		this.skillService = skillService;
	}

	//ADD SKILL
	@PostMapping("/add")
	public ApiResponse<SkillResponseDTO> addSkill(@RequestBody SkillRequestDTO dto) {
		return new ApiResponse<>(true, "Skill Added", skillService.addSkill(dto));
	}

	//GET ALL SKILL
	@GetMapping("/getskill")
	public ApiResponse<List<SkillResponseDTO>> getAllSkill() {
		return new ApiResponse<>(true, "All Skills returned", skillService.getAllSkill());
	}
	//	//GET SKILL BY ID
	//	@GetMapping("/getskill/{id}")
	//	public ApiResponse<SkillResponseDTO> getSkillById(@PathVariable Integer id) {
	//		return new ApiResponse<>(true, "Skill Returned", skillService.getSkillById(id));
	//	}
}
