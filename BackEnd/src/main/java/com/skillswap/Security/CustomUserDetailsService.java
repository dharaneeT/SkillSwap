package com.skillSwap.Security;

import com.skillSwap.Entity.Role;
import com.skillSwap.Entity.User;
import com.skillSwap.Repository.UserRepository;
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
		Role role = user.getRole() == null ? Role.USER : user.getRole();
		boolean isActive = user.getActive() == null || user.getActive();
		return org.springframework.security.core.userdetails.User
			.builder()
			.username(user.getEmail())
			.password(user.getPassword())
			.roles(role.name())
			.disabled(!isActive)
			.build();
	}
}
