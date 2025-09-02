package com.blewandowicz.library_system.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.blewandowicz.library_system.auth.dto.AuthenticationRequest;
import com.blewandowicz.library_system.auth.dto.AuthenticationResponse;
import com.blewandowicz.library_system.auth.dto.RegisterRequest;
import com.blewandowicz.library_system.auth.jwt.JwtUtils;
import com.blewandowicz.library_system.auth.refreshToken.RefreshToken;
import com.blewandowicz.library_system.auth.refreshToken.RefreshTokenService;
import com.blewandowicz.library_system.user.User;
import com.blewandowicz.library_system.user.UserMapper;
import com.blewandowicz.library_system.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
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
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono uzytkownika po uwierzytelnieniu"));
        var jwtToken = jwtUtils.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        AuthenticationResponse authResponse = AuthenticationResponse.builder().jwt(jwtToken).build();

        addRefreshTokenCookie(response, refreshToken.getToken());

        return authResponse;

    }

    @Transactional
    public AuthenticationResponse refreshAuthorizationToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtUtils.extractToken(request);
        String refershToken = extractRefreshToken(request);

        String userEmail = jwtUtils.extractUsernameAlsoIfExpired(accessToken);

        User userFromToken = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika"));

        RefreshToken refreshTokenEntity = refreshTokenService.findToken(refershToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono tokenu"));

        if (!refreshTokenEntity.getUser().getId().equals(userFromToken.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nieprawidłowy refresh token");
        }

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshTokenEntity.getUser());

        String newAccessToken = jwtUtils.generateToken(newRefreshToken.getUser());

        AuthenticationResponse authResponse = AuthenticationResponse.builder().jwt(newAccessToken).build();
        addRefreshTokenCookie(response, newRefreshToken.getToken());
        return authResponse;
    }
}
