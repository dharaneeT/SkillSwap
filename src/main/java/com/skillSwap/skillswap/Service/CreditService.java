package com.skillSwap.skillswap.Service;

import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreditService {

	private final UserRepository userRepository;

	public CreditService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public void addCredits(Integer userId, Integer credits) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		user.setCredits(user.getCredits() + credits);

		userRepository.save(user);
	}
}
