package com.blewandowicz.library_system.auth.exception;

public class UserNotFoundException extends RuntimeException {
    /**
     * Constructs a UserNotFoundException with the specified detail message.
     *
     * <p>Signals that a user lookup failed; this is an unchecked exception.</p>
     *
     * @param message the detail message explaining the missing user (may be {@code null})
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
