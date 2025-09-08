package com.blewandowicz.library_system.auth.refreshToken.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    /**
     * Constructs a new InvalidRefreshTokenException with the specified detail message.
     *
     * @param message detail message describing why the refresh token is considered invalid
     */
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
