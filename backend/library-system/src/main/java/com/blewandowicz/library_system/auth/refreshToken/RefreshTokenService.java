package com.blewandowicz.library_system.auth.refreshToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpiration() / 1000)).build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token wygasł.");
        }
        return token;
    }

    public Optional<RefreshToken> findToken(String refreshTokenString) {
        return refreshTokenRepository.findByToken(refreshTokenString);
    }

    @Transactional
    public int deleteByUser(User user) {
        return refreshTokenRepository.deleteByUser(user);
    }
}
