package com.ptms.app.exception;

/** Thrown when input fails a business rule (duplicate username, invalid status transition, etc.). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
