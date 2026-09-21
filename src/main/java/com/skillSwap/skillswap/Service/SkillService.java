package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Repository.SkillRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkillService {

	@Autowired
	private SkillRepository skillRepository;

	public SkillService(SkillRepository skillRepository) {
		this.skillRepository = skillRepository;
	}

	public Skill addSkill(Skill skill) {
		return skillRepository.save(skill);
	}

	public List<Skill> getAllSkills() {
		return skillRepository.findAll();
	}

	public Skill getSkillById(Integer id) {
		return skillRepository.findById(id).orElseThrow(() -> new RuntimeException("Skill Not Found"));
	}
	//	public String deleteSkill(int id) {
	//		skillRepository.deleteById(id);
	//		return "Skill Deleted Succesfully";
	//	}
}
