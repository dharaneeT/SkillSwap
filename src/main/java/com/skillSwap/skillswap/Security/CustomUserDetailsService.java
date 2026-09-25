package com.skillSwap.skillswap.Security;

import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Repository.UserRepository;
import java.util.ArrayList;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		return new org.springframework.security.core.userdetails.User(//this User class implements UserDetails
			user.getEmail(),
			user.getPassword(), // IMPORTANT: must be encoded
			new ArrayList<>()
		);
	}
}
