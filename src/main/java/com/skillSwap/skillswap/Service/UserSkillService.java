package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Dto.userskill.UserSkillRequestDTO;
import com.skillSwap.skillswap.Dto.userskill.UserSkillResponseDTO;
import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Entity.UserSkill;
import com.skillSwap.skillswap.Exception.UserSkillException;
import com.skillSwap.skillswap.Repository.UserSkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSkillService {

	//	@Autowired
	private final UserSkillRepository userSkillRepository;

	private final UserService userService;
	private final SkillService skillService;
	private final ModelMapper modelMapper;

	public UserSkillService(
		UserSkillRepository userSkillRepository,
		UserService userService,
		SkillService skillService,
		ModelMapper modelMapper
	) {
		this.userSkillRepository = userSkillRepository;
		this.userService = userService;
		this.skillService = skillService;
		this.modelMapper = modelMapper;
	}

	//ADDING USER_SKILL
	@Operation(summary = "ADDING A USER_SKILL")
	public UserSkillResponseDTO addUserSkill(UserSkillRequestDTO dto) {
		User user = userService.getUserEntityById(dto.getUserId());
		Skill skill = skillService.getSkillEntityById(dto.getSkillId());

		UserSkill userSkill = new UserSkill();
		userSkill.setUser(user);
		userSkill.setSkill(skill);
		userSkill.setType(dto.getType());

		UserSkill saved = userSkillRepository.save(userSkill);

		UserSkillResponseDTO response = new UserSkillResponseDTO();
		response.setId(saved.getId());
		response.setUserName(user.getName());
		response.setSkillName(skill.getName());
		response.setType(saved.getType());

		return response;
	}

	//GET USER SKILL
	@Operation(summary = "GET USER_SKILL")
	public List<UserSkillResponseDTO> getUserSkill() {
		return userSkillRepository
			.findAll()
			.stream()
			.map(us -> {
				UserSkillResponseDTO dto = new UserSkillResponseDTO();
				dto.setId(us.getId());
				dto.setUserName(us.getUser().getName());
				dto.setSkillName(us.getSkill().getName());
				dto.setType(us.getType());
				return dto;
			})
			.collect(Collectors.toList());
	}

	//GET USER_SKILL BY ID
	@Operation(summary = "GET USER_SKILL BY ID")
	public UserSkillResponseDTO getUserSkillById(Integer id) {
		UserSkill us = userSkillRepository
			.findById(id)
			.orElseThrow(() -> new UserSkillException("User_Skill is Not Found"));
		return modelMapper.map(us, UserSkillResponseDTO.class);
	}
}
