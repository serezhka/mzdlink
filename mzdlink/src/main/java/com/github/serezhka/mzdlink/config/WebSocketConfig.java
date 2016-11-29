package com.github.serezhka.mzdlink.config;

import com.github.serezhka.mzdlink.controller.websocket.GestureWebSocketHandler;
import com.github.serezhka.mzdlink.controller.websocket.VideoWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final VideoWebSocketHandler videoWebSocketHandler;
    private final GestureWebSocketHandler gestureWebSocketHandler;

    @Value("${config.mzdlink.videoUri}")
    private String videoUri;

    @Value("${config.mzdlink.gestureUri}")
    private String gestureUri;

    @Autowired
    public WebSocketConfig(VideoWebSocketHandler videoWebSocketHandler, GestureWebSocketHandler gestureWebSocketHandler) {
        this.videoWebSocketHandler = videoWebSocketHandler;
        this.gestureWebSocketHandler = gestureWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(videoWebSocketHandler, videoUri).setAllowedOrigins("*");
        webSocketHandlerRegistry.addHandler(gestureWebSocketHandler, gestureUri).setAllowedOrigins("*");
    }
}
