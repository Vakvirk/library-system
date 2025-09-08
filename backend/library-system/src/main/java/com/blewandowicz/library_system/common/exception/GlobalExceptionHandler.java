package com.blewandowicz.library_system.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.blewandowicz.library_system.auth.exception.InvalidCredentialsException;
import com.blewandowicz.library_system.auth.exception.InvalidJWTException;
import com.blewandowicz.library_system.auth.exception.RefreshTokenNotFoundException;
import com.blewandowicz.library_system.auth.exception.UserNotFoundException;
import com.blewandowicz.library_system.auth.exception.UsernameAlreadyExistsException;
import com.blewandowicz.library_system.auth.refreshToken.exception.ExpiredRefreshTokenException;
import com.blewandowicz.library_system.auth.refreshToken.exception.InvalidRefreshTokenException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles InvalidCredentialsException thrown by controllers and returns a 401 Unauthorized response.
     *
     * <p>Produces an ApiError body containing the HTTP status code and the exception message.</p>
     *
     * @param ex the exception raised when provided authentication credentials are invalid
     * @return a ResponseEntity with HTTP 401 (Unauthorized) and an ApiError payload
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Handles InvalidJWTException thrown by controllers and maps it to a 400 Bad Request response.
     *
     * @param ex the caught InvalidJWTException; its message is used as the API error message
     * @return a ResponseEntity containing an ApiError with HTTP status 400 and the exception message
     */
    @ExceptionHandler(InvalidJWTException.class)
    public ResponseEntity<ApiError> handleInvalidJWT(InvalidJWTException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles InvalidRefreshTokenException thrown by controllers and returns a 400 Bad Request
     * response carrying an ApiError with the numeric status and the exception message.
     *
     * @param ex the caught InvalidRefreshTokenException; its message is used as the ApiError message
     * @return a ResponseEntity containing an ApiError with status 400 and the exception message
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles ExpiredRefreshTokenException by returning an HTTP 401 (Unauthorized) response.
     *
     * The response body is an ApiError containing the numeric status code and the exception's message.
     *
     * @return a ResponseEntity<ApiError> with HTTP status 401 and an ApiError payload
     */
    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<ApiError> handleExpiredRefreshToken(ExpiredRefreshTokenException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Handles UserNotFoundException by returning a 404 Not Found response.
     *
     * The response body is an ApiError containing the HTTP status code and the exception message.
     *
     * @return ResponseEntity containing an ApiError with HTTP 404 status and the exception message.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles RefreshTokenNotFoundException and maps it to an HTTP 404 response.
     *
     * @param ex the exception indicating a refresh token was not found; its message is returned in the ApiError body
     * @return a ResponseEntity containing an ApiError with HTTP status 404 and the exception message
     */
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles UsernameAlreadyExistsException and returns HTTP 409 (Conflict) with an ApiError body.
     *
     * @param ex the thrown exception; its message is used as the ApiError message
     * @return ResponseEntity containing an ApiError with status 409 and the exception message
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Build a ResponseEntity containing an ApiError with the given HTTP status and message.
     *
     * @param status  the HTTP status to set on the response
     * @param message the error message to include in the ApiError body
     * @return a ResponseEntity whose body is an ApiError(status.value(), message) and whose HTTP status is {@code status}
     */
    private ResponseEntity<ApiError> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), message));
    }

}
