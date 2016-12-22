package com.github.serezhka.mzdlink.listener;

import com.github.serezhka.mzdlink.adb.AdbClient;
import com.github.serezhka.mzdlink.adb.exception.AdbException;
import com.github.serezhka.mzdlink.adb.listener.DeviceConnectionListener;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class ConnectionListenerTest {

    public static void main(String[] args) throws AdbException {

        AdbClient adbClient = new AdbClient();

        Thread listener = new DeviceConnectionListener(adbClient, 700) {
            @Override
            public void onDeviceConnect() {
                System.err.println("Device connected!");
            }

            @Override
            public void onDeviceDisconnect() {
                System.err.println("Device disconnected!");
            }
        };
        listener.start();
    }
}
