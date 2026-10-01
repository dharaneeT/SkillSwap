package com.skillSwap.websocket;

import com.skillSwap.Entity.User;
import com.skillSwap.Repository.UserRepository;
import com.skillSwap.Security.CustomUserDetailsService;
import com.skillSwap.Security.JwtService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID = "userId";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtHandshakeInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService,
                                   UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler handler, Map<String, Object> attributes) {
        String token = resolveToken(request);
        if (token == null) return reject(response);
        try {
            String email = jwtService.extractUsername(token);
            UserDetails ud = userDetailsService.loadUserByUsername(email);
            if (!ud.isEnabled() || !jwtService.validateToken(token, ud)) return reject(response);
            User user = userRepository.findByEmail(email).orElseThrow();
            attributes.put(USER_ID, user.getId());      // available later via session.getAttributes()
            return true;
        } catch (Exception e) {
            return reject(response);
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler handler, Exception exception) {}

    private String resolveToken(ServerHttpRequest request) {
        List<String> auth = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isEmpty() && auth.get(0).startsWith("Bearer ")) {
            return auth.get(0).substring(7);
        }
        return UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
    }

    private boolean reject(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }
}