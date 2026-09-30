package com.skillSwap.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final Integer expirationMs;

	public JwtService(
		@Value("${jwt.secret}") String base64Secret,
		@Value("${jwt.expiration-ms}") Integer expirationMs
	) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
		this.expirationMs = expirationMs;
	}

	public String generateToken(UserDetails user) {
		String role = user
			.getAuthorities()
			.stream()
			.findFirst()
			.map(GrantedAuthority::getAuthority)
			.orElse("ROLE_USER");
		Date now = new Date();
		return Jwts
			.builder()
			.subject(user.getUsername())
			.claim("role", role)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + expirationMs))
			.signWith(secretKey)
			.compact();
	}

	private Claims parseClaims(String token) {
		// throws ExpiredJwtException / SignatureException / MalformedJwtException
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			Claims c = parseClaims(token);
			return c.getSubject().equals(userDetails.getUsername()) && c.getExpiration().after(new Date());
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public int getExpirationMs() {
		return expirationMs;
	}
	//	@Value("${jwt.expiration-ms}")
	//	//	private Integer expiration;
	//
	//	//	private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
	//
	//	//Generating Token
	//	//	public String generateToken(UserDetails user) {
	//	//		return Jwts //Jwts is a class
	//	//			.builder()
	//	//			.subject(user.getUsername()) //setting the payload
	//	//			.claim("role", userDetails.getAuthorities().toString()) // ADD ROLE
	//	//			.issuedAt(new Date())
	//	//			.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
	//	//			.signWith(secretKey)
	//	//			.compact();
	//	//		//  "sub": "test@gmail.com", //Subject
	//	//		//  "iat": 1790317164,  //iat=Issued At
	//	//		//  "exp": 1790320764   //Expired At
	//	//	}
	//	public String extractUsername(String token) {
	//		Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	//
	//		return claims.getSubject(); //the subject is username AKA email in this instance
	//	}
	//
	//	public boolean validateToken(String token, UserDetails userDetails) {
	//		try {
	//			Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	//
	//			String username = claims.getSubject();
	//
	//			return username.equals(userDetails.getUsername()) && claims.getExpiration().after(new Date());
	//		} catch (Exception e) {
	//			return false;
	//		}
	//	}
}
