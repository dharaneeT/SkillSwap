package com.skillSwap.Service;

import com.skillSwap.Dto.Auth.AuthRequestDTO;
import com.skillSwap.Dto.Auth.AuthResponseDTO;
import com.skillSwap.Dto.Auth.SignupRequestDTO;
import com.skillSwap.Entity.Role;
import com.skillSwap.Entity.User;
import com.skillSwap.Exception.DuplicateResourceException;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.Security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authManager;
	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		AuthenticationManager authManager,
		UserDetailsService userDetailsService,
		JwtService jwtService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authManager = authManager;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponseDTO signup(SignupRequestDTO req) {
		String email = req.getEmail().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("Email is already registered");
		}

		User user = new User();
		user.setName(req.getName().trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setRole(Role.USER);
		user.setActive(true);
		user.setCredits(10);
		userRepository.save(user);

		//  publish UserRegisteredEvent here

		return toResponse(userDetailsService.loadUserByUsername(email));
	}

	public AuthResponseDTO login(AuthRequestDTO req) {
		Authentication auth = authManager.authenticate(
			new UsernamePasswordAuthenticationToken(req.getEmail().trim(), req.getPassword())
		);
		return toResponse((UserDetails) auth.getPrincipal());
	}

	private AuthResponseDTO toResponse(UserDetails ud) {
		String role = ud.getAuthorities().iterator().next().getAuthority();
		return new AuthResponseDTO(
			jwtService.generateToken(ud),
			"Bearer",
			jwtService.getExpirationMs(),
			ud.getUsername(),
			role
		);
	}
}
