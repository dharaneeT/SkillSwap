package com.skillSwap.skillswap.Security;

import com.skillSwap.skillswap.Entity.Role;
import com.skillSwap.skillswap.Entity.User;
import com.skillSwap.skillswap.Repository.UserRepository;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(role.name())
                .disabled(Boolean.FALSE.equals(user.getActive()))
                .build();
	}
}
