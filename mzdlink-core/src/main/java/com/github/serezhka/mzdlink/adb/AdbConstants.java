package com.github.serezhka.mzdlink.adb;

import java.util.regex.Pattern;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public interface AdbConstants {

    // ADB v. 1.0.32 keywords
    String ADB = "adb";
    String SHELL = "shell";
    String PUSH = "push";
    String CHMOD = "chmod";
    String GETPROP = "getprop";
    String DUMPSYS = "dumpsys";
    String WINDOW = "window";
    String FORWARD = "forward";
    String REMOVE_ALL = "--remove-all";
    String DISPLAYS = "displays";
    String MKDIR = "mkdir";
    String GET_STATE = "get-state";
    String KILL_SERVER = "kill-server";
    String START_SERVER = "start-server";
    String WAIT_FOR_DEVICE = "wait-for-device";

    // ADB v. 1.0.32 messages
    String MESSAGE_NO_DEVICES_FOUND = "error: no devices found";
    String MESSAGE_DEVICE = "device";

    // Android constants
    String PROPERTY_ABI = "ro.product.cpu.abi";
    String PROPERTY_SDK = "ro.build.version.sdk";
    String PROPERTY_RELEASE = "ro.build.version.release";

    Pattern PATTERN_VIEWPORT_SIZE = Pattern.compile(" cur=(.*?)x(.*?) ");
}
