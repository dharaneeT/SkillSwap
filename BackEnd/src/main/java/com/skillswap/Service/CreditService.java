package com.skillSwap.Service;

import com.skillSwap.Entity.User;
import com.skillSwap.Repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Service;

@Service
public class CreditService {

	private final UserRepository userRepository;

	public CreditService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Operation(summary = "ADD CREDITS")
	public void addCredits(Integer userId, Integer credits) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		user.setCredits(user.getCredits() + credits);

		userRepository.save(user);
	}
}
