package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Dto.skill.SkillRequestDTO;
import com.skillSwap.skillswap.Dto.skill.SkillResponseDTO;
import com.skillSwap.skillswap.Entity.Skill;
import com.skillSwap.skillswap.Repository.SkillRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkillService {

	@Autowired
	private SkillRepository skillRepository;

	public SkillService(SkillRepository skillRepository, ModelMapper modelMapper) {
		this.skillRepository = skillRepository;
		this.modelMapper = modelMapper;
	}

	@Autowired
	private ModelMapper modelMapper;

	//CREATE A SKILL

	public SkillResponseDTO addSkill(SkillRequestDTO dto) {
		Skill skill = modelMapper.map(dto, Skill.class);
		Skill saved = skillRepository.save(skill);
		return modelMapper.map(saved, SkillResponseDTO.class);
	}

	//GET ALL SKILLS
	public List<SkillResponseDTO> getAllSkill() {
		return skillRepository
			.findAll()
			.stream()
			.map(skill -> modelMapper.map(skill, SkillResponseDTO.class))
			.collect(Collectors.toList());
	}

	//	//GET SKILL BY ID
	//	public SkillResponseDTO getSkillById(Integer id) {
	//		return modelMapper.map(skillRepository.findById(id), SkillResponseDTO.class);
	//	}

	//INTERNAL METHOD
	public Skill getSkillEntityById(Integer id) {
		return skillRepository.findById(id).orElseThrow(() -> new RuntimeException("Skill not found"));
	}

	//	public Skill addSkill(Skill skill) {
	//		return skillRepository.save(skill);
	//	}
	//
	//	public List<Skill> getAllSkills() {
	//		return skillRepository.findAll();
	//	}
	//
	public SkillResponseDTO getSkillById(Integer id) {
		Skill skill = skillRepository.findById(id).orElseThrow(() -> new RuntimeException("Skill not found"));
		return modelMapper.map(skill, SkillResponseDTO.class);
	}
	//	public String deleteSkill(int id) {
	//		skillRepository.deleteById(id);
	//		return "Skill Deleted Succesfully";
	//	}
}
