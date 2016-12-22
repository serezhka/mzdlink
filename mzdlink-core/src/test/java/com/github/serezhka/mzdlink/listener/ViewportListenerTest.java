package com.github.serezhka.mzdlink.listener;

import com.github.serezhka.mzdlink.adb.AdbClient;
import com.github.serezhka.mzdlink.adb.DeviceViewport;
import com.github.serezhka.mzdlink.adb.exception.AdbException;
import com.github.serezhka.mzdlink.adb.listener.DeviceViewportListener;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class ViewportListenerTest {

    public static void main(String[] args) throws AdbException {

        AdbClient adbClient = new AdbClient();

        Thread listener = new DeviceViewportListener(adbClient, 700) {
            @Override
            public void onDeviceViewportChanged(DeviceViewport deviceViewport) throws AdbException {
                System.err.println(deviceViewport);
            }
        };
        listener.start();
    }
}
