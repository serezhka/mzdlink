package com.github.serezhka.mzdlink.websocket;

import com.github.serezhka.mzdlink.service.RemoteControlService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Controller
public class MzdlinkWebsocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = Logger.getLogger(MzdlinkWebsocketHandler.class);

    private final RemoteControlService remoteControlService;

    private WebSocketSession session;

    @Autowired
    public MzdlinkWebsocketHandler(RemoteControlService remoteControlService) {
        this.remoteControlService = remoteControlService;
    }

    @PostConstruct
    public void init() {
        remoteControlService.setDeviceScreenListener(this::sendMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        remoteControlService.processGesture(message.asBytes());
    }

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
