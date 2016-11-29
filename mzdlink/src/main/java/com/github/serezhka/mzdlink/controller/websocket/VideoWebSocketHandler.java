package com.github.serezhka.mzdlink.controller.websocket;

import com.github.serezhka.mzdlink.service.RemoteControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Controller
public class VideoWebSocketHandler extends OneSessionWebSocketHandler {

    private final RemoteControlService remoteControlService;

    @Autowired
    public VideoWebSocketHandler(RemoteControlService remoteControlService) {
        this.remoteControlService = remoteControlService;
    }

    @PostConstruct
    public void init() {
        remoteControlService.setDeviceScreenListener(this::sendMessage);
    }
}
