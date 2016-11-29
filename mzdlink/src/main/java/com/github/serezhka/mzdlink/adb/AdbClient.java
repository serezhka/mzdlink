package com.github.serezhka.mzdlink.adb;

import com.github.serezhka.mzdlink.adb.exception.AdbException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static com.github.serezhka.mzdlink.adb.AdbConstants.*;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
@Component
public class AdbClient {

    private static final Logger LOGGER = Logger.getLogger(AdbClient.class);

    public AdbClient() throws AdbException {
        execCommand(ADB, KILL_SERVER);
        execCommand(ADB, START_SERVER);
    }

    public Process waitForDevice() throws AdbException {
        return runCommand(ADB, WAIT_FOR_DEVICE);
    }

    public boolean isDeviceConnected() throws AdbException {
        return execCommand(ADB, GET_STATE).trim().equals(MESSAGE_DEVICE);
    }

    public String getDeviceAbi() throws AdbException {
        return getDeviceProperty(PROPERTY_ABI);
    }

    public String getDeviceSdk() throws AdbException {
        return getDeviceProperty(PROPERTY_SDK);
    }

    public String getDeviceRelease() throws AdbException {
        return getDeviceProperty(PROPERTY_RELEASE);
    }

    public DeviceViewport getDeviceViewport() throws AdbException {
        try {
            Process process = runCommand(ADB, SHELL, DUMPSYS, WINDOW, DISPLAYS);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Matcher viewportMatcher;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                viewportMatcher = PATTERN_VIEWPORT_SIZE.matcher(line);
                if (viewportMatcher.find()) {
                    return new DeviceViewport(
                            Integer.parseInt(viewportMatcher.group(1)),
                            Integer.parseInt(viewportMatcher.group(2)));
                }
            }
            throw new AdbException("Unable to get display orientation.");
        } catch (IOException e) {
            throw new AdbException(e);
        }
    }

    public void makeDirectory(String directory) throws AdbException {
        execCommand(ADB, SHELL, MKDIR, directory, "2>/dev/null");
    }

    public void uploadFile(String localPath, String remotePath) throws AdbException {
        execCommand(ADB, PUSH, localPath, remotePath);
    }

    public void chmod(String rights, String path) throws AdbException {
        execCommand(ADB, SHELL, CHMOD, rights, path);
    }

    public void forward(String local, String remote) throws AdbException {
        execCommand(ADB, FORWARD, local, remote);
    }

    public Process shell(String... command) throws AdbException {
        return runCommand(Stream.concat(Arrays.stream(new String[]{ADB, SHELL}), Arrays.stream(command)).toArray(String[]::new));
    }

    private String getDeviceProperty(String propertyName) throws AdbException {
        String value = execCommand(ADB, SHELL, GETPROP, propertyName).trim();
        if (MESSAGE_NO_DEVICES_FOUND.equals(value)) throw new AdbException(MESSAGE_NO_DEVICES_FOUND);
        return value;
    }

    private String execCommand(String... command) throws AdbException {
        Process process = runCommand(command);
        try {
            String response = inputStreamToString(process.getInputStream());
            LOGGER.debug("Response for adb command: <" + Arrays.toString(command) + "> is <" + response + ">");
            return response;
        } catch (IOException e) {
            throw new AdbException(e);
        }
    }

    private Process runCommand(String... command) throws AdbException {
        LOGGER.debug("Executing adb command: <" + Arrays.toString(command) + ">");
        try {
            return new ProcessBuilder(command).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new AdbException(e);
        }
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }
}
