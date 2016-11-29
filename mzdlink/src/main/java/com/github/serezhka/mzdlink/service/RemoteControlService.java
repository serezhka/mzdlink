package com.github.serezhka.mzdlink.service;

import com.github.serezhka.mzdlink.adb.AdbClient;
import com.github.serezhka.mzdlink.adb.DeviceViewport;
import com.github.serezhka.mzdlink.adb.exception.AdbException;
import com.github.serezhka.mzdlink.adb.listener.DeviceViewportListener;
import com.github.serezhka.mzdlink.openstf.Header;
import com.github.serezhka.mzdlink.openstf.MinicapImageReceiver;
import com.github.serezhka.mzdlink.openstf.MinitouchGestureSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Service
public class RemoteControlService {

    private static final Logger LOGGER = Logger.getLogger(RemoteControlService.class);

    private final AdbClient adbClient;

    private MinicapImageReceiver minicapImageReceiver;
    private MinitouchGestureSender minitouchGestureSender;
    private DeviceViewportListener deviceViewportListener;
    private DeviceScreenListener deviceScreenListener;
    private Process minicapProcess;
    private Process minitouchProcess;

    @Autowired
    public RemoteControlService(AdbClient adbClient) {
        this.adbClient = adbClient;
    }

    @PostConstruct
    private void init() {
        new Thread(() -> {
            while (!Thread.interrupted()) {

                try {
                    adbClient.waitForDevice().waitFor();

                    deviceViewportListener = new DeviceViewportListener(adbClient, getViewportDelay) {
                        @Override
                        public void onDeviceViewportChanged(DeviceViewport deviceViewport) throws AdbException {

                            adbClient.forward("tcp:" + minicapPort, "localabstract:minicap");
                            adbClient.forward("tcp:" + minitouchPort, "localabstract:minitouch");

                            // Start minicap
                            killProcess(minicapProcess);
                            int targetWidth = deviceViewport.isLandscape() ? minicapTargetWidth : minicapTargetHeight;
                            int targetHeight = deviceViewport.isLandscape() ? minicapTargetHeight : minicapTargetWidth;
                            String minicapArgs = "\"-P " + deviceViewport.getWidth() + "x" + deviceViewport.getHeight()
                                    + "@" + targetWidth + "x" + targetHeight + "/0\"";
                            minicapProcess = adbClient.shell("\"LD_LIBRARY_PATH=/data/local/tmp\"", "/data/local/tmp/minicap", minicapArgs);

                            // Start minitouch
                            if (minitouchProcess == null || !minitouchProcess.isAlive()) {
                                killProcess(minitouchProcess);
                                minitouchProcess = adbClient.shell("/data/local/tmp/minitouch");
                            }
                        }
                    };

                    minicapImageReceiver = new MinicapImageReceiver(new InetSocketAddress(minicapIp, minicapPort), minicapReconnectDelay, minicapBufferSize) {
                        @Override
                        public void onReceive(ByteBuffer byteBuffer) {
                            if (deviceScreenListener != null) deviceScreenListener.onScreenUpdate(byteBuffer);
                        }

                        @Override
                        public void onHeaderReceived(Header header) {
                            LOGGER.info("Minicap's header received : " + header);
                        }
                    };

                    minitouchGestureSender = new MinitouchGestureSender(new InetSocketAddress(minitouchIp, minitouchPort), minitouchReconnectDelay, minitouchBufferSize) {
                    };

                    deviceViewportListener.start();
                    minicapImageReceiver.start();
                    minitouchGestureSender.start();
                    deviceViewportListener.join();

                } catch (InterruptedException | AdbException e) {
                    LOGGER.info("remote control service", e);
                } finally {
                    if (minicapImageReceiver != null) {
                        minicapImageReceiver.interrupt();
                        minicapImageReceiver = null;
                    }
                    if (minitouchGestureSender != null) {
                        minitouchGestureSender.interrupt();
                        minitouchGestureSender = null;
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

    public void processGesture(byte[] gesture) {
        if (minitouchGestureSender != null) minitouchGestureSender.sendGesture(gesture);
    }

    public void setDeviceScreenListener(DeviceScreenListener deviceScreenListener) {
        this.deviceScreenListener = deviceScreenListener;
    }

    @FunctionalInterface
    public interface DeviceScreenListener {
        void onScreenUpdate(ByteBuffer byteBuffer);
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

    @Value("${config.minitouch.ip}")
    private String minitouchIp;

    @Value("${config.minitouch.port}")
    private int minitouchPort;

    @Value("${config.minitouch.reconnectDelay}")
    private int minitouchReconnectDelay;

    @Value("${config.minitouch.bufferSize}")
    private int minitouchBufferSize;
}
