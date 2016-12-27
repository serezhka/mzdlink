package com.github.serezhka.mzdlink.service;

import com.github.serezhka.mzdlink.adb.DeviceViewport;
import com.github.serezhka.mzdlink.adb.listener.DeviceConnectionListener;
import com.github.serezhka.mzdlink.adb.listener.DeviceViewportListener;
import com.github.serezhka.mzdlink.socket.ReconnectableSocketFactory;
import com.github.serezhka.mzdlink.socket.minicap.Header;
import com.github.serezhka.mzdlink.socket.minicap.MinicapImageReceiver;
import com.github.serezhka.mzdlink.socket.minitouch.MinitouchGestureSender;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.Stream;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Component
public class RemoteControlService {

    private static final Logger LOGGER = Logger.getLogger(RemoteControlService.class);

    private final JadbConnection jadbConnection;
    private final ReconnectableSocketFactory reconnectableSocketFactory;

    private MinicapImageReceiver minicapImageReceiver;
    private MinitouchGestureSender minitouchGestureSender;

    private DeviceViewportListener deviceViewportListener;
    private DeviceScreenListener deviceScreenListener;
    private Thread minicapSocketClient;
    private Thread minitouchSocketClient;
    private Thread minicapProcess;
    private Thread minitouchProcess;

    @Autowired
    public RemoteControlService(JadbConnection jadbConnection,
                                ReconnectableSocketFactory reconnectableSocketFactory) {
        this.jadbConnection = jadbConnection;
        this.reconnectableSocketFactory = reconnectableSocketFactory;
    }

    @PostConstruct
    private void init() {

        try {
            jadbConnection.getHostVersion();
        } catch (IOException | JadbException e) {
            throw new RuntimeException("It seems ADB not running", e);
        }

        minicapImageReceiver = new MinicapImageReceiver() {

            @Override
            public void onReceive(ByteBuf imageFrame) {
                if (deviceScreenListener != null) {
                    deviceScreenListener.onScreenUpdate(imageFrame);
                } else imageFrame.release();
            }

            @Override
            public void onHeaderReceived(Header header) {
                LOGGER.info("Minicap's header received : " + header);
            }
        };

        minitouchGestureSender = new MinitouchGestureSender() {
        };

        new DeviceConnectionListener(jadbConnection, getDeviceDelay) {
            @Override
            public void onDeviceConnect(JadbDevice device) {
                LOGGER.info("Device " + device.getSerial() + " connected!");

                try {
                    jadbConnection.setForwarding("tcp:" + minicapPort, "localabstract:minicap");
                    jadbConnection.setForwarding("tcp:" + minitouchPort, "localabstract:minitouch");
                } catch (IOException | JadbException e) {
                    LOGGER.warn("Unable to set socket connection forwardings", e);
                    return;
                }

                if (minitouchProcess == null || !minitouchProcess.isAlive()) {
                    if (minitouchProcess != null) minitouchProcess.interrupt();
                    minitouchProcess = createThread(device, "/data/local/tmp/minitouch");
                    minitouchProcess.start();
                }

                deviceViewportListener = new DeviceViewportListener(device, getViewportDelay) {
                    @Override
                    public void onDeviceViewportChanged(DeviceViewport deviceViewport) {
                        LOGGER.info("Device viewport change: " + deviceViewport);

                        if (minicapProcess != null) minicapProcess.interrupt();
                        int targetWidth = deviceViewport.isLandscape() ? minicapTargetWidth : minicapTargetHeight;
                        int targetHeight = deviceViewport.isLandscape() ? minicapTargetHeight : minicapTargetWidth;
                        minicapProcess = createThread(device, "LD_LIBRARY_PATH=/data/local/tmp",
                                "/data/local/tmp/minicap",
                                "-P", String.format("%dx%d@%dx%d/0", deviceViewport.getWidth(), deviceViewport.getHeight(), targetWidth, targetHeight),
                                "-Q", minicapImageQuality + "");
                        minicapProcess.start();
                    }
                };

                minicapSocketClient = reconnectableSocketFactory.connect(
                        new InetSocketAddress(minicapIp, minicapPort), minicapBufferSize, minicapReconnectDelay, minicapImageReceiver);
                minitouchSocketClient = reconnectableSocketFactory.connect(
                        new InetSocketAddress(minitouchIp, minitouchPort), minitouchBufferSize, minitouchReconnectDelay, minitouchGestureSender);

                deviceViewportListener.start();
                minicapSocketClient.start();
                minitouchSocketClient.start();
            }

            @Override
            public void onDeviceDisconnect(JadbDevice device) {
                LOGGER.info("Device " + device.getSerial() + " disconnected!");
                try {
                    jadbConnection.removeForwardings();
                } catch (IOException | JadbException e) {
                    LOGGER.warn("Unable to remove socket connection forwardings", e);
                }
                if (deviceViewportListener != null) {
                    deviceViewportListener.interrupt();
                    deviceViewportListener = null;
                }
                if (minicapSocketClient != null) {
                    minicapSocketClient.interrupt();
                    minicapSocketClient = null;
                }
                if (minitouchSocketClient != null) {
                    minitouchSocketClient.interrupt();
                    minitouchSocketClient = null;
                }
            }
        }.start();
    }

    public void processGesture(ByteBuf gesture) {
        if (minitouchGestureSender != null) {
            minitouchGestureSender.sendGesture(gesture);
        } else gesture.release();
    }

    public void setDeviceScreenListener(DeviceScreenListener deviceScreenListener) {
        this.deviceScreenListener = deviceScreenListener;
    }

    @FunctionalInterface
    public interface DeviceScreenListener {
        void onScreenUpdate(ByteBuf image);
    }

    private Thread createThread(JadbDevice device, String command, String... args) {
        return new Thread() {
            InputStream inputStream;

            @Override
            public void run() {
                try {
                    Stream.readAll(inputStream = device.executeShell(command, args), Charset.forName("UTF-8"));
                } catch (IOException | JadbException e) {
                    LOGGER.debug(e);
                }
            }

            @Override
            public void interrupt() {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.debug(e);
                }
                super.interrupt();
            }
        };
    }

    @Value("${config.adb.getDeviceDelay}")
    private int getDeviceDelay;

    @Value("${config.adb.getViewportDelay}")
    private int getViewportDelay;

    @Value("${config.minicap.ip}")
    private String minicapIp;

    @Value("${config.minicap.port}")
    private int minicapPort;

    @Value("${config.minicap.bufferSize}")
    private int minicapBufferSize;

    @Value("${config.minicap.reconnectDelay}")
    private int minicapReconnectDelay;

    @Value("${config.minicap.targetWidth}")
    private int minicapTargetWidth;

    @Value("${config.minicap.targetHeight}")
    private int minicapTargetHeight;

    @Value("${config.minicap.imageQuality}")
    private int minicapImageQuality;

    @Value("${config.minitouch.ip}")
    private String minitouchIp;

    @Value("${config.minitouch.port}")
    private int minitouchPort;

    @Value("${config.minitouch.bufferSize}")
    private int minitouchBufferSize;

    @Value("${config.minitouch.reconnectDelay}")
    private int minitouchReconnectDelay;
}
