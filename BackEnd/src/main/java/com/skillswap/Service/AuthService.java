package com.skillSwap.Service;

import com.skillSwap.Dto.Auth.AuthRequestDTO;
import com.skillSwap.Dto.Auth.AuthResponseDTO;
import com.skillSwap.Dto.Auth.SignupRequestDTO;
import com.skillSwap.Entity.CreditTxType;
import com.skillSwap.Entity.Role;
import com.skillSwap.Entity.User;
import com.skillSwap.Event.UserRegisteredEvent;
import com.skillSwap.Exception.DuplicateResourceException;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.Security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authManager;
	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;
	private final CreditWalletService wallet;
	private final ApplicationEventPublisher events;
	private final int signupBonus;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		AuthenticationManager authManager,
		UserDetailsService userDetailsService,
		JwtService jwtService,
		CreditWalletService wallet,
		ApplicationEventPublisher events,
		@Value("${skillswap.credits.signup-bonus}") int signupBonus
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authManager = authManager;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
		this.wallet = wallet;
		this.events = events;
		this.signupBonus = signupBonus;
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
		user.setCredits(0); // the bonus goes through the wallet so it is logged
		User saved = userRepository.save(user);

		wallet.credit(saved.getId(), signupBonus, CreditTxType.SIGNUP_BONUS, null, "Signup bonus");
		events.publishEvent(new UserRegisteredEvent(saved.getName(), saved.getEmail(), signupBonus));

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
