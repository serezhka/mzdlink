package com.github.serezhka.mzdlink.config;

import com.github.serezhka.mzdlink.websocket.MzdlinkWebsocketHandler;
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

    private final MzdlinkWebsocketHandler mzdlinkWebsocketHandler;

    @Value("${config.mzdlink.wsUri}")
    private String wsUri;

    @Autowired
    public WebSocketConfig(MzdlinkWebsocketHandler mzdlinkWebsocketHandler) {
        this.mzdlinkWebsocketHandler = mzdlinkWebsocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(mzdlinkWebsocketHandler, wsUri).setAllowedOrigins("*");
    }
}
