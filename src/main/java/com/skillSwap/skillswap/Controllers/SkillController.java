package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Dto.response.ApiResponse;
import com.skillSwap.skillswap.Dto.skill.SkillRequestDTO;
import com.skillSwap.skillswap.Dto.skill.SkillResponseDTO;
import com.skillSwap.skillswap.Service.SkillService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<ApiResponse<SkillResponseDTO>> addSkill(@Valid @RequestBody SkillRequestDTO dto) {
		SkillResponseDTO data = skillService.addSkill(dto);

		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Skill Added", data));
	}

	//GET ALL SKILL
	@GetMapping("/getskill")
	public ResponseEntity<ApiResponse<List<SkillResponseDTO>>> getAllSkill() {
		return ResponseEntity.ok(new ApiResponse<>(true, "All users", skillService.getAllSkill()));
	}
	//	//GET SKILL BY ID
	//	@GetMapping("/getskill/{id}")
	//	public ApiResponse<SkillResponseDTO> getSkillById(@PathVariable Integer id) {
	//		return new ApiResponse<>(true, "Skill Returned", skillService.getSkillById(id));
	//	}
}
