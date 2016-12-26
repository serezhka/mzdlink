package com.github.serezhka.mzdlink.adb.listener;

import org.apache.log4j.Logger;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.IOException;
import java.util.List;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public abstract class DeviceConnectionListener extends Thread {

    private static final Logger LOGGER = Logger.getLogger(DeviceConnectionListener.class);

    private final JadbConnection jadbConnection;
    private final int delay;

    private JadbDevice trackingDevice;

    public DeviceConnectionListener(JadbConnection jadbConnection, int delay) {
        this.jadbConnection = jadbConnection;
        this.delay = delay;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                if (trackingDevice == null) {
                    List<JadbDevice> devices = jadbConnection.getDevices();
                    for (JadbDevice device : devices) {
                        if (device.getState().equals(JadbDevice.State.Device)) {
                            onDeviceConnect(trackingDevice = device);
                            break;
                        }
                    }
                } else {
                    if (!trackingDevice.getState().equals(JadbDevice.State.Device)) {
                        onDeviceDisconnect(trackingDevice);
                        trackingDevice = null;
                    }
                }
            } catch (IOException | JadbException e) {
                LOGGER.error(e);
                return;
            }

            try {
                sleep(delay);
            } catch (InterruptedException ignored) {
                return;
            }
        }
    }

    public abstract void onDeviceConnect(JadbDevice device);

    public abstract void onDeviceDisconnect(JadbDevice device);
}
