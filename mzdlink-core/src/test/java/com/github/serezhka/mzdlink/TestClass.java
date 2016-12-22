package com.github.serezhka.mzdlink;

import com.github.serezhka.mzdlink.adb.exception.AdbException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Sergei Fedorov (sergei.fedorov@t-systems.ru)
 * @since 30.11.2016
 */
@SuppressWarnings("Duplicates")
public class TestClass {

    public static void main(String[] args) throws AdbException {

        System.out.println(String.format("-P %dx%d@%dx%d/0 -Q %d", 1920, 1080, 800, 480, 70));

    }

    private static String execCommand(String... command) throws AdbException {
        Process process = runCommand(command);
        try {
            return inputStreamToString(process.getInputStream());
        } catch (IOException e) {
            throw new AdbException(e);
        }
    }

    private static Process runCommand(String... command) throws AdbException {
        try {
            return new ProcessBuilder(command).inheritIO().redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new AdbException(e);
        }
    }

    private static String inputStreamToString(InputStream inputStream) throws IOException {
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


