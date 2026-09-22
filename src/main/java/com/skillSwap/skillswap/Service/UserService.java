package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Dto.user.UserProfileDTO;
import com.skillSwap.skillswap.Dto.user.UserRequestDTO;
import com.skillSwap.skillswap.Dto.user.UserResponseDTO;
import com.skillSwap.skillswap.Entity.SkillType;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Exception.UserNotFoundException;
import com.skillSwap.skillswap.Repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	public UserService(UserRepository userRepository, ModelMapper modelMapper) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
	}

	@Autowired
	private final UserRepository userRepository;

	@Autowired
	private final ModelMapper modelMapper;

	//CREATE USERS
	public UserResponseDTO createUser(UserRequestDTO dto) {
		User user = modelMapper.map(dto, User.class);
		user.setCredits(10);
		User saved = userRepository.save(user);
		return modelMapper.map(saved, UserResponseDTO.class);
	}

	//GET ALL USERS
	public List<UserResponseDTO> getAllUsers() {
		return userRepository
			.findAll()
			.stream()
			.map(user -> modelMapper.map(user, UserResponseDTO.class))
			.collect(Collectors.toList());
	}

	//GET USER BY ID
	public UserResponseDTO getUserById(Integer id) {
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not Found"));
		return modelMapper.map(user, UserResponseDTO.class);
	}

	// INTERNAL ENTITY ACCESS (FOR OTHER SERVICES) //AI
	public User getUserEntityById(Integer id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
	}

	//FULL PROFILE OF THE  USER
	public UserProfileDTO getUserProfile(Integer userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		UserProfileDTO dto = new UserProfileDTO();

		dto.setId(user.getId());
		dto.setName(user.getName());
		dto.setEmail(user.getEmail());
		dto.setCredits(user.getCredits());

		// OFFERED SKILLS
		List<String> offered = user
			.getSkills()
			.stream()
			.filter(us -> us.getType() == SkillType.OFFERED)
			.map(us -> us.getSkill().getName())
			.toList();

		// WANTED SKILLS
		List<String> wanted = user
			.getSkills()
			.stream()
			.filter(us -> us.getType() == SkillType.WANTED)
			.map(us -> us.getSkill().getName())
			.toList();

		dto.setOfferedSkills(offered);
		dto.setWantedSkills(wanted);

		return dto;
	}
	//            .
	//            .
	//            .
	//            .
	//            .
	//            .
	//            .
	//            .
	//            .
	//            .
	//            .
	//            ..
	//            .
	//            .
	//            .
	//            .
	//            .
	//	public User createUser(User user) {
	//		return userRepository.save(user);
	//	}
	//
	//	public List<User> getAllUsers() {
	//		return userRepository.findAll();
	//	}
	//
	//	public User getUserById(Integer id) {
	//		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
	//	}
	//	//    public String deleteUser(int id){
	//	//        userRepository.deleteById(id);
	//	//        return "User deleted Successfully";
	//	//    }
}
