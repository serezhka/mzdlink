package com.github.serezhka.mzdlink.adb;

import com.github.serezhka.mzdlink.adb.DeviceConnectionListener;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class ConnectionListenerTest {

    public static void main(String[] args) {

        Thread listener = new DeviceConnectionListener(new JadbConnection(), 700) {
            @Override
            public void onDeviceConnect(JadbDevice device) {
                System.err.println("Device " + device.getSerial() + " connected!");
            }

            @Override
            public void onDeviceDisconnect(JadbDevice device) {
                System.err.println("Device " + device.getSerial() + " disconnected!");
            }
        };
        listener.start();
    }
}
