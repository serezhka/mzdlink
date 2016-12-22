package com.github.serezhka.mzdlink.service;

import com.github.serezhka.mzdlink.adb.AdbClient;
import com.github.serezhka.mzdlink.adb.DeviceViewport;
import com.github.serezhka.mzdlink.adb.exception.AdbException;
import com.github.serezhka.mzdlink.adb.listener.DeviceViewportListener;
import com.github.serezhka.mzdlink.socket.ReconnectableSocketFactory;
import com.github.serezhka.mzdlink.socket.minicap.Header;
import com.github.serezhka.mzdlink.socket.minicap.MinicapImageReceiver;
import com.github.serezhka.mzdlink.socket.minitouch.MinitouchGestureSender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.EventLoopGroup;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Component
public class RemoteControlService {

    private static final Logger LOGGER = Logger.getLogger(RemoteControlService.class);

    private final AdbClient adbClient;
    private final EventLoopGroup workerGroup;
    private final ReconnectableSocketFactory reconnectableSocketFactory;

    private MinicapImageReceiver minicapImageReceiver;
    private MinitouchGestureSender minitouchGestureSender;

    private DeviceViewportListener deviceViewportListener;
    private DeviceScreenListener deviceScreenListener;
    private Thread minicapSocketClient;
    private Thread minitouchSocketClient;
    private Process minicapProcess;
    private Process minitouchProcess;

    @Autowired
    public RemoteControlService(AdbClient adbClient,
                                EventLoopGroup workerGroup,
                                ReconnectableSocketFactory reconnectableSocketFactory) {
        this.adbClient = adbClient;
        this.workerGroup = workerGroup;
        this.reconnectableSocketFactory = reconnectableSocketFactory;
    }

    @PostConstruct
    private void init() {

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

        new Thread(() -> {
            while (!Thread.interrupted()) {

                try {
                    LOGGER.info("Waiting for device..");
                    adbClient.waitForDevice().waitFor();
                    LOGGER.info("Device connected!");

                    adbClient.forward("tcp:" + minicapPort, "localabstract:minicap");
                    adbClient.forward("tcp:" + minitouchPort, "localabstract:minitouch");

                    // Start minitouch
                    if (minitouchProcess == null || !minitouchProcess.isAlive()) {
                        killProcess(minitouchProcess);
                        minitouchProcess = adbClient.shell("/data/local/tmp/minitouch");
                    }

                    deviceViewportListener = new DeviceViewportListener(adbClient, getViewportDelay) {
                        @Override
                        public void onDeviceViewportChanged(DeviceViewport deviceViewport) throws AdbException {

                            LOGGER.info("Device viewport change: " + deviceViewport);

                            // Start minicap
                            killProcess(minicapProcess);
                            int targetWidth = deviceViewport.isLandscape() ? minicapTargetWidth : minicapTargetHeight;
                            int targetHeight = deviceViewport.isLandscape() ? minicapTargetHeight : minicapTargetWidth;
                            String minicapArgs = String.format("-P %dx%d@%dx%d/0 -Q %d", deviceViewport.getWidth(), deviceViewport.getHeight(), targetWidth, targetHeight, minicapImageQuality);
                            minicapProcess = adbClient.shell("LD_LIBRARY_PATH=/data/local/tmp", "/data/local/tmp/minicap", minicapArgs);
                        }
                    };

                    minicapSocketClient = reconnectableSocketFactory.connect(new InetSocketAddress(minicapIp, minicapPort), minicapReconnectDelay, minicapImageReceiver);
                    minitouchSocketClient = reconnectableSocketFactory.connect(new InetSocketAddress(minitouchIp, minitouchPort), minitouchReconnectDelay, minitouchGestureSender);

                    deviceViewportListener.start();
                    minicapSocketClient.start();
                    minitouchSocketClient.start();
                    deviceViewportListener.join();

                    adbClient.stopForwarding();

                } catch (InterruptedException | AdbException e) {
                    LOGGER.info("remote control service", e);
                } finally {
                    if (minicapSocketClient != null) {
                        minicapSocketClient.interrupt();
                        minicapSocketClient = null;
                    }
                    if (minitouchSocketClient != null) {
                        minitouchSocketClient.interrupt();
                        minitouchSocketClient = null;
                    }
                    if (deviceViewportListener != null) {
                        deviceViewportListener.interrupt();
                        deviceViewportListener = null;
                    }
                    killProcess(minicapProcess);
                    killProcess(minitouchProcess);
                }
            }
        }).start();
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

    private void killProcess(Process process) {
        if (process != null) {
            process.destroy();
            //noinspection StatementWithEmptyBody
            while (process.isAlive()) {
            }
        }
    }

    @Value("${config.adb.getViewportDelay}")
    private int getViewportDelay;

    @Value("${config.minicap.ip}")
    private String minicapIp;

    @Value("${config.minicap.port}")
    private int minicapPort;

    @Value("${config.minicap.reconnectDelay}")
    private int minicapReconnectDelay;

    @Value("${config.minicap.bufferSize}")
    private int minicapBufferSize;

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

    @Value("${config.minitouch.reconnectDelay}")
    private int minitouchReconnectDelay;

    @Value("${config.minitouch.bufferSize}")
    private int minitouchBufferSize;
}
