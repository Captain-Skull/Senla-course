package com.senla.pas.exception;

public class PasException extends RuntimeException {
    public PasException(String message) {
        super(message);
    }

    public PasException(String message, Throwable cause) {
        super(message, cause);
    }
}
