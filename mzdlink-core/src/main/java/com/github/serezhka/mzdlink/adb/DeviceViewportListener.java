package com.github.serezhka.mzdlink.adb;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public abstract class DeviceViewportListener extends Thread {

    private static final Pattern PATTERN_VIEWPORT = Pattern.compile(" cur=(.*?)x(.*?) ");

    private final JadbDevice jadbDevice;
    private final int delay;

    private DeviceViewport deviceViewport;

    public DeviceViewportListener(JadbDevice jadbDevice, int delay) {
        this.jadbDevice = jadbDevice;
        this.delay = delay;
    }

    @Override
    public void run() {

        DeviceViewport tmp = null;

        while (!interrupted()) {
            try (InputStream inputStream = jadbDevice.executeShell("dumpsys", "window", "displays")) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                Matcher viewportMatcher;
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    viewportMatcher = PATTERN_VIEWPORT.matcher(line);
                    if (viewportMatcher.find()) {
                        tmp = new DeviceViewport(
                                Integer.parseInt(viewportMatcher.group(1)),
                                Integer.parseInt(viewportMatcher.group(2)));
                    }
                }

                if (tmp != null && (deviceViewport == null || deviceViewport.isLandscape() != tmp.isLandscape())) {
                    deviceViewport = tmp;
                    onDeviceViewportChanged(deviceViewport);
                }
            } catch (IOException | JadbException e) {
                return;
            }

            try {
                sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public abstract void onDeviceViewportChanged(DeviceViewport deviceViewport);
}
