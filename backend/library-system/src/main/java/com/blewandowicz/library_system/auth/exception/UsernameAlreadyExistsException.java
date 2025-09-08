package com.blewandowicz.library_system.auth.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a UsernameAlreadyExistsException with the specified detail message.
     *
     * <p>Use this exception to indicate an attempt to create or register a user with a
     * username that is already present in the system.</p>
     *
     * @param message a descriptive message explaining why the username is considered duplicate
     */
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
