package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Dto.userskill.UserSkillRequestDTO;
import com.skillSwap.skillswap.Dto.userskill.UserSkillResponseDTO;
import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Entity.UserSkill;
import com.skillSwap.skillswap.Repository.UserSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSkillService {

	@Autowired
	private final UserSkillRepository userSkillRepository;

	private final UserService userService;
	private final SkillService skillService;

	public UserSkillService(
		SkillService skillService,
		UserService userService,
		UserSkillRepository userSkillRepository
	) {
		this.skillService = skillService;
		this.userService = userService;
		this.userSkillRepository = userSkillRepository;
	}

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
}
