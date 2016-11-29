package com.github.serezhka.mzdlink.adb.listener;

import com.github.serezhka.mzdlink.adb.AdbClient;
import com.github.serezhka.mzdlink.adb.DeviceViewport;
import com.github.serezhka.mzdlink.adb.exception.AdbException;
import org.apache.log4j.Logger;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public abstract class DeviceViewportListener extends Thread {

    private static final Logger LOGGER = Logger.getLogger(DeviceViewportListener.class);

    private final AdbClient adbClient;
    private final int delay;

    private DeviceViewport deviceViewport;

    public DeviceViewportListener(AdbClient adbClient, int delay) {
        this.adbClient = adbClient;
        this.delay = delay;
    }

    @Override
    public void run() {
        DeviceViewport tmp;
        while (!interrupted()) {
            try {
                tmp = adbClient.getDeviceViewport();
                if (deviceViewport == null || deviceViewport.isLandscape() != tmp.isLandscape()) {
                    deviceViewport = tmp;
                    onDeviceViewportChanged(deviceViewport);
                }
            } catch (AdbException e) {
                LOGGER.debug("device disconnected ?", e);
                return;
            }
            try {
                sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public abstract void onDeviceViewportChanged(DeviceViewport deviceViewport) throws AdbException;
}
