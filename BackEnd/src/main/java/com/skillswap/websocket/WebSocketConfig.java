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
    private final StudyRoomWebSocketHandler studyRoomHandler;
    private final StudyRoomHandshakeInterceptor studyRoomInterceptor;

    public WebSocketConfig(
            ChatWebSocketHandler handler,
            JwtHandshakeInterceptor jwtInterceptor,
            StudyRoomWebSocketHandler studyRoomHandler,
            StudyRoomHandshakeInterceptor studyRoomInterceptor
    ) {
        this.handler = handler;
        this.jwtInterceptor = jwtInterceptor;
        this.studyRoomHandler = studyRoomHandler;
        this.studyRoomInterceptor = studyRoomInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 1-to-1 chat (unchanged)
        registry.addHandler(handler, "/ws").addInterceptors(jwtInterceptor).setAllowedOrigins("http://localhost:5173"); //  Vite dev server

        // study rooms: JWT check first, then "is this user an ACTIVE member of that room?"
        registry
                .addHandler(studyRoomHandler, "/ws/rooms")
                .addInterceptors(jwtInterceptor, studyRoomInterceptor)
                .setAllowedOrigins("http://localhost:5173");
    }
}