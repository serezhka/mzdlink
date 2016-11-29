package com.github.serezhka.mzdlink.controller.websocket;

import com.github.serezhka.mzdlink.service.RemoteControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Controller
public class GestureWebSocketHandler extends OneSessionWebSocketHandler {

    private final RemoteControlService remoteControlService;

    @Autowired
    public GestureWebSocketHandler(RemoteControlService remoteControlService) {
        this.remoteControlService = remoteControlService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        remoteControlService.processGesture(message.asBytes());
    }
}
