package com.blewandowicz.library_system.auth.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    /**
     * Constructs a new RefreshTokenNotFoundException with the specified detail message.
     *
     * @param message the detail message describing the missing refresh token
     */
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
