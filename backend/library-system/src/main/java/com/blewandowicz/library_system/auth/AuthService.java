package com.blewandowicz.library_system.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.blewandowicz.library_system.auth.dto.AuthenticationRequest;
import com.blewandowicz.library_system.auth.dto.AuthenticationResponse;
import com.blewandowicz.library_system.auth.dto.RegisterRequest;
import com.blewandowicz.library_system.auth.exception.InvalidCredentialsException;
import com.blewandowicz.library_system.auth.exception.RefreshTokenNotFoundException;
import com.blewandowicz.library_system.auth.exception.UserNotFoundException;
import com.blewandowicz.library_system.auth.exception.UsernameAlreadyExistsException;
import com.blewandowicz.library_system.auth.jwt.JwtUtils;
import com.blewandowicz.library_system.auth.refreshToken.RefreshToken;
import com.blewandowicz.library_system.auth.refreshToken.RefreshTokenService;
import com.blewandowicz.library_system.auth.refreshToken.exception.InvalidRefreshTokenException;
import com.blewandowicz.library_system.user.User;
import com.blewandowicz.library_system.user.UserMapper;
import com.blewandowicz.library_system.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final RefreshTokenService refreshTokenService;

    /**
     * Adds an HTTP-only "refreshToken" cookie to the given response.
     *
     * The cookie is named "refreshToken", contains the provided token value, has
     * a max-age of 7 days, SameSite=Strict, and HttpOnly enabled. The cookie is
     * written to the response via the "Set-Cookie" header.
     *
     * @param response     the HttpServletResponse to which the cookie will be added
     * @param refreshToken the refresh token value to store in the cookie
     */
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict").build();
        response.setHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Retrieves the value of the "refreshToken" cookie from the given HTTP request.
     *
     * @return the refresh token string if the "refreshToken" cookie is present; otherwise {@code null}
     */
    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Registers a new user account.
     *
     * <p>Validates that the email from {@code request} is not already taken, hashes the provided
     * password, maps the request to a User entity, persists it, and returns HTTP 200 with a
     * success message.
     *
     * @param request  registration data (name, lastName, email, password); the plaintext password
     *                 will be hashed before persistence
     * @param response HTTP response (used by flow for cookie handling elsewhere)
     * @return HTTP 200 OK with a success message when registration completes
     * @throws UsernameAlreadyExistsException if an account with the given email already exists
     */
    @Transactional
    public ResponseEntity<String> register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UsernameAlreadyExistsException("Ten adres email jest już zarejestrowany");
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        RegisterRequest withHashedPassword = new RegisterRequest(
                request.name(),
                request.lastName(),
                request.email(),
                hashedPassword);
        User user = UserMapper.INSTANCE.registerToUser(withHashedPassword);
        userRepository.save(user);
        return ResponseEntity.ok("Pomyślnie zarejestrowano.");
    }

    /**
     * Authenticates a user and issues authentication tokens.
     *
     * Attempts to authenticate with the provided credentials, loads the user record,
     * generates a JWT access token and a refresh token, sets the refresh token in an
     * HTTP-only cookie on the given response, and returns an AuthenticationResponse
     * containing the JWT.
     *
     * @param request  authentication credentials (email and password)
     * @param response HTTP response used to add the refresh-token cookie
     * @return an AuthenticationResponse containing the issued JWT access token
     * @throws InvalidCredentialsException if authentication fails due to bad credentials
     * @throws UserNotFoundException      if a user with the provided email does not exist
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request,
            HttpServletResponse response) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika w bazie danych"));
        var jwtToken = jwtUtils.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        AuthenticationResponse authResponse = AuthenticationResponse.builder().jwt(jwtToken).build();

        addRefreshTokenCookie(response, refreshToken.getToken());

        return authResponse;

    }

    /**
     * Refreshes the user's access and refresh tokens using the access token (may be expired) and the refresh token from the request.
     *
     * <p>The method extracts the access token and the refresh token (from a cookie), derives the user from the access token,
     * verifies the refresh token exists, is not expired, and belongs to that user, then issues a new access token and a new
     * refresh token. The new refresh token is written to an HttpOnly cookie on the response.
     *
     * @return an AuthenticationResponse containing the newly generated access (JWT) token
     * @throws UserNotFoundException if no user is found for the email extracted from the access token
     * @throws RefreshTokenNotFoundException if the refresh token cookie is missing or the token cannot be found in storage
     * @throws InvalidRefreshTokenException if the refresh token does not belong to the user identified by the access token
     */
    @Transactional
    public AuthenticationResponse refreshAuthorizationToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtUtils.extractToken(request);
        String refershToken = extractRefreshToken(request);

        String userEmail = jwtUtils.extractUsernameAlsoIfExpired(accessToken);

        User userFromToken = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Nie znaleziono użytkownika w bazie danych"));

        RefreshToken refreshTokenEntity = refreshTokenService.findToken(refershToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Nie znaleziono tokenu w bazie danych"));

        if (!refreshTokenEntity.getUser().getId().equals(userFromToken.getId())) {
            throw new InvalidRefreshTokenException("Nieprawidłowy refresh token");
        }

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshTokenEntity.getUser());

        String newAccessToken = jwtUtils.generateToken(newRefreshToken.getUser());

        AuthenticationResponse authResponse = AuthenticationResponse.builder().jwt(newAccessToken).build();
        addRefreshTokenCookie(response, newRefreshToken.getToken());
        return authResponse;
    }
}
