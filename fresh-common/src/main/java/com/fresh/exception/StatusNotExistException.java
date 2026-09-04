package com.fresh.exception;

public class StatusNotExistException extends RuntimeException {
    public StatusNotExistException(String message) {
        super(message);
    }
}
