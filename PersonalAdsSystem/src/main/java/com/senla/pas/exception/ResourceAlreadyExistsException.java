package com.senla.pas.exception;

public class ResourceAlreadyExistsException extends IllegalArgumentException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
