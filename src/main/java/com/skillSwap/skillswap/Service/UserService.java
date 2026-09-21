package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Autowired
	private UserRepository userRepository;

	public User createUser(User user) {
		return userRepository.save(user);
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User getUserById(int id) {
		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not Found"));
	}
	//    public String deleteUser(int id){
	//        userRepository.deleteById(id);
	//        return "User deleted Successfully";
	//    }
}
