package com.blewandowicz.library_system.auth.refreshToken;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blewandowicz.library_system.user.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    /**
 * Deletes all refresh tokens associated with the given user.
 *
 * @param user the user whose refresh tokens should be removed
 * @return the number of refresh token records deleted
 */
Integer deleteByUser(User user);

    /**
 * Retrieves a RefreshToken entity by its token string.
 *
 * @param token the exact token value to look up
 * @return an Optional containing the matching RefreshToken, or empty if none found
 */
Optional<RefreshToken> findByToken(String token);
}
