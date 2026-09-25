package com.skillSwap.skillswap.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final String SECRET = "Be yourself; everyone else is taken";
	private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

	//Generating Token
	public String generateToken(String username) {
		return Jwts
			.builder()
			.subject(username) //setting the payload
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
			.signWith(secretKey)
			.compact();
		//  "sub": "test@gmail.com", //Subject
		//  "iat": 1790317164,  //iat=Issued At
		//  "exp": 1790320764   //Expired At
	}

	//
	public String extractUsername(String token) {
		Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

		return claims.getSubject();
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

			String username = claims.getSubject();

			return username.equals(userDetails.getUsername()) && claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}
}
