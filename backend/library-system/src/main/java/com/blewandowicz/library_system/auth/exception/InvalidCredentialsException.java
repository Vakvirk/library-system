package com.blewandowicz.library_system.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    /**
     * Constructs a new InvalidCredentialsException with the specified detail message.
     *
     * @param message the detail message describing the authentication failure
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
