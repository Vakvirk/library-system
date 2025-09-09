package com.blewandowicz.library_system.auth;

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

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict").build();
        response.setHeader("Set-Cookie", cookie.toString());
    }

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

    @Transactional
    public AuthenticationResponse refreshAuthorizationToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new InvalidRefreshTokenException("Brak refresh tokenu.");
        }
        RefreshToken refreshTokenEntity = refreshTokenService.findToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Nie znaleziono refresh tokena w bazie"));

        User user = userRepository.findByEmail(refreshTokenEntity.getUser().getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        "Użytkownik skojarzony z tym refresh tokenem nie znajduje się w bazie danych"));

        if (!user.isEnabled() || !user.isActive()) {
            throw new RuntimeException("Ten użytkownik jest wyłączony lub niektywny");
        }

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        String newAccessToken = jwtUtils.generateToken(user);

        AuthenticationResponse authResponse = AuthenticationResponse.builder().jwt(newAccessToken).build();
        addRefreshTokenCookie(response, newRefreshToken.getToken());

        return authResponse;

    }
}
