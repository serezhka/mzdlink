package com.github.serezhka.mzdlink.listener;

import com.github.serezhka.mzdlink.adb.DeviceViewport;
import com.github.serezhka.mzdlink.adb.listener.DeviceViewportListener;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.IOException;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class ViewportListenerTest {

    public static void main(String[] args) throws IOException, JadbException {

        JadbDevice jadbDevice = new JadbConnection().getDevices().get(0);

        Thread listener = new DeviceViewportListener(jadbDevice, 700) {
            @Override
            public void onDeviceViewportChanged(DeviceViewport deviceViewport) {
                System.err.println(deviceViewport);
            }
        };
        listener.start();
    }
}
