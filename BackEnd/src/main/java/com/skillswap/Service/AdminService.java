package com.skillswap.Service;

import com.skillswap.Dto.user.UserResponseDTO;
import com.skillswap.Entity.Role;
import com.skillswap.Entity.User;
import com.skillswap.Exception.SkillNotFoundException;
import com.skillswap.Exception.UserNotFoundException;
import com.skillswap.Repository.SkillRepository;
import com.skillswap.Repository.UserRepository;
import com.skillswap.Security.CurrentUserService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

	private final UserRepository userRepository;
	private final SkillRepository skillRepository;
	private final CurrentUserService currentUserService;
	private final ModelMapper modelMapper;

	public AdminService(
		UserRepository userRepository,
		SkillRepository skillRepository,
		CurrentUserService currentUserService,
		ModelMapper modelMapper
	) {
		this.userRepository = userRepository;
		this.skillRepository = skillRepository;
		this.currentUserService = currentUserService;
		this.modelMapper = modelMapper;
	}

	@Transactional
	public List<UserResponseDTO> listUsers() {
		return userRepository.findAll().stream().map(u -> modelMapper.map(u, UserResponseDTO.class)).toList();
	}

	@Transactional
	public void setActive(Integer userId, boolean active) {
		User target = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		if (target.getId().equals(currentUserService.getCurrentUser().getId())) {
			throw new IllegalArgumentException("You cannot deactivate your own account");
		}
		target.setActive(active);
	}

	@Transactional
	public void setRole(Integer userId, Role role) {
		User target = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		if (target.getId().equals(currentUserService.getCurrentUser().getId())) {
			throw new IllegalArgumentException("You cannot change your own role");
		}
		target.setRole(role);
	}

	@Transactional
	public void deleteSkill(Integer skillId) {
		if (!skillRepository.existsById(skillId)) throw new SkillNotFoundException(skillId);
		skillRepository.deleteById(skillId);
	}
}
