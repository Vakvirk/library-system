package com.blewandowicz.library_system.auth.refreshToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.blewandowicz.library_system.auth.refreshToken.exception.ExpiredRefreshTokenException;
import com.blewandowicz.library_system.auth.refreshToken.exception.InvalidRefreshTokenException;
import com.blewandowicz.library_system.common.config.JwtProperties;
import com.blewandowicz.library_system.user.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        if (user == null) {
            throw new InvalidRefreshTokenException("User nie może być null.");
        }
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpiration() / 1000)).build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token == null) {
            throw new InvalidRefreshTokenException("Token nie może być null.");
        }
        if (token.getExpiryDate() == null) {
            throw new InvalidRefreshTokenException("Token musi posidadać datę wygaśnięcia.");
        }
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException("Refresh token wygasł.");
        }

        return token;
    }

    public Optional<RefreshToken> findToken(String refreshTokenString) {
        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            return Optional.empty();
        }
        return refreshTokenRepository.findByToken(refreshTokenString);
    }

    @Transactional
    public Integer deleteByUser(User user) {
        if (user == null) {
            throw new InvalidRefreshTokenException("User nie może być null.");
        }

        return refreshTokenRepository.deleteByUser(user);
    }
}
