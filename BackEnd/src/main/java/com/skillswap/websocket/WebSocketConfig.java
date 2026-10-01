package com.skillSwap.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final ChatWebSocketHandler handler;
	private final JwtHandshakeInterceptor jwtInterceptor;

	public WebSocketConfig(ChatWebSocketHandler handler, JwtHandshakeInterceptor jwtInterceptor) {
		this.handler = handler;
		this.jwtInterceptor = jwtInterceptor;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/ws").addInterceptors(jwtInterceptor).setAllowedOrigins("http://localhost:5173"); // your Vite dev server
	}
}
