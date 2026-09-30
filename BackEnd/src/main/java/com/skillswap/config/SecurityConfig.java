package com.skillSwap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillSwap.Dto.errorResponse.ErrorResponseDTO;
import com.skillSwap.Security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;
	private final ObjectMapper objectMapper;

	public SecurityConfig(
		JwtAuthFilter jwtAuthFilter,
		UserDetailsService userDetailsService,
		ObjectMapper objectMapper
	) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
		this.objectMapper = objectMapper;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration c = new CorsConfiguration();
		c.setAllowedOrigins(List.of("http://localhost:5173"));
		c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", c);
		return source;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth ->
				auth
					.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/skillswap/v1/auth/**")
					.permitAll()
					.requestMatchers("/skillswap/v1/admin/**")
					.hasRole("ADMIN")
					.anyRequest()
					.authenticated()
			)
			.exceptionHandling(ex ->
				ex
					.authenticationEntryPoint((req, res, e) ->
						writeError(res, HttpStatus.UNAUTHORIZED, "Authentication required or token invalid/expired")
					)
					.accessDeniedHandler((req, res, e) ->
						writeError(res, HttpStatus.FORBIDDEN, "You do not have permission to do this")
					)
			)
			.authenticationProvider(authenticationProvider())
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	private void writeError(HttpServletResponse res, HttpStatus status, String message) throws IOException {
		res.setStatus(status.value());
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(
			res.getOutputStream(),
			new ErrorResponseDTO(false, message, status.value(), LocalDateTime.now())
		);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider p = new DaoAuthenticationProvider();
		p.setUserDetailsService(userDetailsService);
		p.setPasswordEncoder(passwordEncoder());
		return p;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	//	private final JwtAuthFilter jwtAuthFilter;
	//	private final UserDetailsService userDetailsService;
	//
	//	public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
	//		this.jwtAuthFilter = jwtAuthFilter;
	//		this.userDetailsService = userDetailsService;
	//	}
	//
	//	@Bean
	//	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	//		http
	//			.csrf(csrf -> csrf.disable())
	//			.authorizeHttpRequests(auth ->
	//				auth
	//					// PUBLIC
	//					.requestMatchers(
	//						"/swagger-ui.html/**",
	//						"/v3/api-docs/**",
	//						"/skillswap/v1/users/add/**",
	//						"/skillswap/v1/auth/**"
	//					)
	//					.permitAll()
	//					// SECURED
	//					.requestMatchers("/skillswap/v1/admin/**")
	//					.hasRole("ADMIN")
	//					// USER + ADMIN
	//					.requestMatchers("/skillswap/v1/user/**")
	//					.hasAnyRole("USER", "ADMIN")
	//					.anyRequest()
	//					.authenticated()
	//			)
	//			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
	//
	//		http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
	//
	//		return http.build();
	//	}
	//
	//	@Bean
	//	public PasswordEncoder passwordEncoder() {
	//		return new BCryptPasswordEncoder();
	//	}
	//
	//	@Bean
	//	public AuthenticationProvider authenticationProvider() {
	//		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	//
	//		provider.setUserDetailsService(userDetailsService);
	//		provider.setPasswordEncoder(passwordEncoder());
	//
	//		return provider;
	//	}
	//
	//	@Bean
	//	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
	//		return config.getAuthenticationManager();
	//	}
}
