package com.blewandowicz.library_system.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blewandowicz.library_system.auth.dto.AuthenticationRequest;
import com.blewandowicz.library_system.auth.dto.AuthenticationResponse;
import com.blewandowicz.library_system.auth.dto.RegisterRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Authenticates a user with the provided credentials.
     *
     * Delegates authentication to the AuthService and returns an HTTP 200 response
     * containing an AuthenticationResponse (typically including access/refresh tokens and user info).
     *
     * @param request the authentication credentials (e.g., username/email and password)
     * @return HTTP 200 with the resulting AuthenticationResponse on successful authentication
     */
    @GetMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.authenticate(request, response));
    }

    /**
     * Register a new user account.
     *
     * Delegates registration to AuthService using the provided registration data.
     *
     * @param request registration details (e.g., username, email, password)
     * @return a ResponseEntity containing a result message and an appropriate HTTP status
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        return authService.register(request, response);
    }

    /**
     * Refreshes the client's access token and returns a new AuthenticationResponse.
     *
     * <p>Exchanges the refresh token (provided in the incoming HttpServletRequest) for a fresh
     * access token and related authentication metadata. The operation may also modify the
     * HttpServletResponse (e.g., setting cookies or headers) as part of the token refresh flow.
     *
     * @return HTTP 200 with an AuthenticationResponse containing the renewed access token and related data
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshAuthorizationToken(request, response));
    }
}
