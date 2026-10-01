package com.skillSwap.Service;

import com.skillSwap.Dto.user.UserResponseDTO;
import com.skillSwap.Entity.Role;
import com.skillSwap.Entity.User;
import com.skillSwap.Exception.SkillNotFoundException;
import com.skillSwap.Exception.UserNotFoundException;
import com.skillSwap.Repository.SkillRepository;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.Security.CurrentUserService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
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

    @CacheEvict(cacheNames = "skillSearch", allEntries = true)
	@Transactional
	public void deleteSkill(Integer skillId) {
		if (!skillRepository.existsById(skillId)) throw new SkillNotFoundException(skillId);
		skillRepository.deleteById(skillId);
	}
}
