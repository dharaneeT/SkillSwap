package com.skillSwap.skillswap.Controllers;

import com.skillSwap.skillswap.Entity.SkillType;
import com.skillSwap.skillswap.Entity.UserSkill;
import com.skillSwap.skillswap.Service.UserSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/skillswap/v1/user-skill")
public class UserSkillController {

	@Autowired
	private UserSkillService userSkillService;

	public UserSkillController(UserSkillService userSkillService) {
		this.userSkillService = userSkillService;
	}

	@PostMapping
	public UserSkill addUserSkill(@RequestParam int userId, @RequestParam int skillId, @RequestParam SkillType type) {
		return userSkillService.addUserSkill(userId, skillId, type);
	}
}
