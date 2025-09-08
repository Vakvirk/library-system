package com.blewandowicz.library_system.common.exception;

import java.time.LocalDateTime;

public record ApiError(int status, String message, LocalDateTime timestamp) {
    /**
     * Creates an ApiError with the given status and message, setting the timestamp to the current date-time.
     *
     * @param status  numeric status code representing the error
     * @param message human-readable error message
     */
    public ApiError(int status, String message) {
        this(status, message, LocalDateTime.now());
    }

}
