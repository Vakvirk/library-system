package com.blewandowicz.library_system.auth.refreshToken.exception;

public class ExpiredRefreshTokenException extends RuntimeException {
    /**
     * Constructs a new ExpiredRefreshTokenException with the specified detail message.
     *
     * @param message a message describing why the refresh token is considered expired
     */
    public ExpiredRefreshTokenException(String message) {
        super(message);
    }
}
