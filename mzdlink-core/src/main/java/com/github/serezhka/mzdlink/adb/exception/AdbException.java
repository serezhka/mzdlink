package com.github.serezhka.mzdlink.adb.exception;

/**
 * @author Sergei Fedorov (serezhka@xakep.ru)
 */
public class AdbException extends Exception {
    public AdbException(String message) {
        super(message);
    }

    public AdbException(Throwable cause) {
        super(cause);
    }

    public AdbException(String message, Throwable cause) {
        super(message, cause);
    }
}
