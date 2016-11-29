package com.github.serezhka.mzdlink.adb.listener;

import com.github.serezhka.mzdlink.adb.AdbClient;
import com.github.serezhka.mzdlink.adb.exception.AdbException;
import org.apache.log4j.Logger;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Deprecated
public abstract class DeviceConnectionListener extends Thread {

    private static final Logger LOGGER = Logger.getLogger(DeviceConnectionListener.class);

    private final AdbClient adbClient;
    private final int delay;

    public DeviceConnectionListener(AdbClient adbClient, int delay) {
        this.adbClient = adbClient;
        this.delay = delay;
    }

    @Override
    public void run() {
        boolean deviceConnected = false;
        while (!interrupted()) {
            try {
                boolean adbDeviceConnected = adbClient.isDeviceConnected();
                if (!deviceConnected) {
                    if (adbDeviceConnected) {
                        deviceConnected = true;
                        onDeviceConnect();
                    }
                } else if (!adbDeviceConnected) {
                    deviceConnected = false;
                    onDeviceDisconnect();
                }
            } catch (AdbException e) {
                LOGGER.error(e);
                if (deviceConnected) onDeviceDisconnect();
            }
            try {
                sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public abstract void onDeviceConnect();

    public abstract void onDeviceDisconnect();
}
