package com.market.domain.chat.config;

import com.market.domain.chat.WebSocketAuthHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@EnableWebSocket
@Configuration
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketAuthHandler webSocketAuthHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register the WebSocket handler for the /chat endpoint
        registry.addHandler(webSocketAuthHandler, "/chat")
            .setAllowedOrigins("*"); // Allow requests from all origins
    }
}