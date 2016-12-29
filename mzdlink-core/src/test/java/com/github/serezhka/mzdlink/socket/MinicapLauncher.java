package com.github.serezhka.mzdlink.socket;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class MinicapLauncher {

    private static final int PORT = 1114;

    public static void main(String[] args) throws IOException, JadbException {

        JadbConnection jadbConnection = new JadbConnection();
        System.out.println("adb version " + jadbConnection.getHostVersion());

        List<JadbDevice> devices = jadbConnection.getDevices();
        if (devices.isEmpty()) {
            System.err.println("no devices");
            return;
        }

        JadbDevice jadbDevice = devices.get(0);

        new Thread(() -> {
            try (InputStream inputStream = jadbDevice.executeShell("LD_LIBRARY_PATH=/data/local/tmp",
                    "/data/local/tmp/minicap",
                    "-P", String.format("%dx%d@%dx%d/0", 1920, 1080, 800, 480),
                    "-Q", 80 + "");
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                 Stream<String> output = bufferedReader.lines()) {
                output.forEach(System.out::println);
            } catch (IOException | JadbException e) {
                e.printStackTrace();
            }
        }).start();

        jadbConnection.setForwarding("tcp:" + PORT, "localabstract:minicap");
    }
}
