package com.skillSwap.Security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String header = request.getHeader("Authorization");

		//  Authorization header missing
		//       ↓
		//JwtAuthFilter does nothing
		//       ↓
		//continue
		//       ↓
		//Spring Security sees endpoint requires authentication
		//       ↓
		//401 Unauthorized

		String path = request.getServletPath();
		if (path.startsWith("/skillswap/v1/auth/")) {
			filterChain.doFilter(request, response);
			return;
		}
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = header.substring(7);
		try {
			String username = jwtService.extractUsername(token);
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails user = userDetailsService.loadUserByUsername(username);
				if (user.isEnabled() && jwtService.validateToken(token, user)) {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						user,
						null,
						user.getAuthorities()
					);
					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			}
		} catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
			SecurityContextHolder.clearContext(); // invalid token → stay anonymous
		}

		//		//if Token exist
		//		String token = header.substring(7); //beacuse the word "Bearer has 7 char"
		//		String username = jwtService.extractUsername(token); //extract username
		//
		//		//Check user is already validated, so we don't have to validate again
		//		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
		//			UserDetails user = userDetailsService.loadUserByUsername(username);
		//
		//			if (jwtService.validateToken(token, user)) {
		//				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
		//					user,
		//					null,
		//					user.getAuthorities()
		//				);
		//
		//				SecurityContextHolder.getContext().setAuthentication(auth);
		//			}
		//		}

		filterChain.doFilter(request, response);
	}
}
//initial progress completed
