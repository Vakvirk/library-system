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

    /**
     * Creates and persists a new refresh token for the given user.
     *
     * Deletes any existing refresh tokens for the user, generates a new token string (UUID),
     * sets its expiry based on configured JWT refresh expiration, and saves it.
     *
     * @param user the token owner; must not be null
     * @return the newly created and persisted RefreshToken
     * @throws InvalidRefreshTokenException if {@code user} is null
     */
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

    /**
     * Validates that a refresh token is present and not expired, and returns it.
     *
     * Checks that the provided token and its expiry date are non-null and that the expiry
     * date is not in the past. Returns the same token instance if all checks pass.
     *
     * @param token the refresh token to validate
     * @return the validated refresh token
     * @throws InvalidRefreshTokenException if the token is null or has a null expiry date
     * @throws ExpiredRefreshTokenException if the token's expiry date is before the current time
     */
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

    /**
     * Finds a refresh token by its string value.
     *
     * If the provided string is null or blank this method returns {@code Optional.empty()}.
     * Otherwise it returns the repository result: an {@code Optional} containing the matching
     * RefreshToken if found, or empty if not present.
     *
     * @param refreshTokenString the token string to look up (may be null or blank)
     * @return an Optional with the matching RefreshToken, or empty when the input is blank/null or no token is found
     */
    public Optional<RefreshToken> findToken(String refreshTokenString) {
        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            return Optional.empty();
        }
        return refreshTokenRepository.findByToken(refreshTokenString);
    }

    /**
     * Deletes all refresh tokens associated with the given user.
     *
     * @param user the user whose refresh tokens should be removed; must not be null
     * @return the number of refresh tokens deleted
     * @throws InvalidRefreshTokenException if {@code user} is null
     */
    @Transactional
    public Integer deleteByUser(User user) {
        if (user == null) {
            throw new InvalidRefreshTokenException("User nie może być null.");
        }

        return refreshTokenRepository.deleteByUser(user);
    }
}
