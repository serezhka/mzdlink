package com.github.serezhka.mzdlink.controller.websocket;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Controller
public abstract class OneSessionWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = Logger.getLogger(OneSessionWebSocketHandler.class);

    private WebSocketSession session;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        synchronized (this) {
            if (this.session == null) {
                this.session = session;
                LOGGER.info("Client " + session.getRemoteAddress() + " connected to " + session.getLocalAddress() + session.getUri());
            } else {
                session.close();
                LOGGER.info("Client " + session.getRemoteAddress() + " connection to " + session.getLocalAddress() + session.getUri() + " declined.");
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        synchronized (this) {
            if (this.session != null && session.getId().equals(this.session.getId())) {
                this.session = null;
                LOGGER.info("Client " + session.getRemoteAddress() + " disconnected from " + session.getLocalAddress() + session.getUri());
            }
        }
    }

    protected void sendMessage(String message) {
        synchronized (this) {
            if (session == null) return;
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
    }

    protected void sendMessage(ByteBuffer message) {
        sendMessage(Base64.getEncoder().encodeToString(message.array()));
    }
}
