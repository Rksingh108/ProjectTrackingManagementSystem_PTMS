package com.ptms.app.exception;

/** Thrown when a user tries to perform an action their role doesn't allow. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}