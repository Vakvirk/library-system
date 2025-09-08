package com.blewandowicz.library_system.auth.refreshToken;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blewandowicz.library_system.user.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Integer deleteByUser(User user);

    Optional<RefreshToken> findByToken(String token);
}
