package com.ptms.app.exception;

/** Thrown when a lookup by id finds nothing. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}