package com.skillSwap.Controllers;

import com.skillSwap.Dto.response.ApiResponse;
import com.skillSwap.Dto.skill.SkillRequestDTO;
import com.skillSwap.Dto.skill.SkillResponseDTO;
import com.skillSwap.Service.SkillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skillswap/v1/skill")
@Tag(name = "Skill API", description = "Skill management APIs")
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
		return ResponseEntity.ok(new ApiResponse<>(true, "All SKills", skillService.getAllSkill()));
	}

	//GET SKILL BY ID
	@GetMapping("/getskill/{id}")
	public ApiResponse<SkillResponseDTO> getSkillById(@PathVariable Integer id) {
		return new ApiResponse<>(true, "Skill Returned", skillService.getSkillById(id));
	}
}
