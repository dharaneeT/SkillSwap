package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Entity.SkillType;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Entity.UserSkill;
import com.skillSwap.skillswap.Repository.UserSkillRepository;
import com.skillSwap.skillswap.Service.SkillService;
import com.skillSwap.skillswap.Service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserSkillService {

	private final UserSkillRepository userSkillRepository;
	private final UserService userService;
	private final SkillService skillService;

	public UserSkillService(
		UserSkillRepository userSkillRepository,
		UserService userService,
		SkillService skillService
	) {
		this.userSkillRepository = userSkillRepository;
		this.userService = userService;
		this.skillService = skillService;
	}

	public UserSkill addUserSkill(Integer userId, Integer skillId, SkillType type) {
		User user = userService.getUserById(userId);
		Skill skill = skillService.getSkillById(skillId);

		UserSkill userSkill = new UserSkill();
		userSkill.setUser(user);
		userSkill.setSkill(skill);
		userSkill.setType(type);

		return userSkillRepository.save(userSkill);
	}
}
