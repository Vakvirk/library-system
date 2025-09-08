package com.blewandowicz.library_system.auth.exception;

public class InvalidJWTException extends RuntimeException {
    /**
     * Constructs a new InvalidJWTException with the specified detail message.
     *
     * @param message human-readable detail explaining why the JWT is considered invalid
     */
    public InvalidJWTException(String message) {
        super(message);
    }
}
