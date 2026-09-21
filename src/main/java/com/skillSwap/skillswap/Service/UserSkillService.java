package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Entity.SkillType;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Entity.UserSkill;
import com.skillSwap.skillswap.Repository.UserSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSkillService {

	@Autowired
	private UserSkillRepository userSkillRepository;

	@Autowired
	private UserService userService;

	public UserSkillService(
		UserSkillRepository userSkillRepository,
		UserService userService,
		SkillService skilService
	) {
		this.userSkillRepository = userSkillRepository;
		this.userService = userService;
		this.skillService = skilService;
	}

	@Autowired
	private SkillService skillService;


    //Adding User
	public UserSkill addUserSkill(int userId, int skillId, SkillType type) {
		User user = userService.getUserById(userId);
		Skill skill = skillService.getSkillById(skillId);
		UserSkill userSkill = new UserSkill();
		userSkill.setUser(user);
		userSkill.setSkill(skill);
		userSkill.setType(type);
		return userSkillRepository.save(userSkill);
	}


}
