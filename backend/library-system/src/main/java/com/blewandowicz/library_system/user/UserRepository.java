package com.blewandowicz.library_system.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    /**
 * Finds a user by their email address.
 *
 * @param email the email address to search for
 * @return an Optional containing the matching User if present, otherwise Optional.empty()
 */
Optional<User> findByEmail(String email);

    /**
 * Checks whether a user with the given email exists in the repository.
 *
 * @param email the email address to check for an existing user
 * @return true if a user with the specified email exists, false otherwise
 */
Boolean existsByEmail(String email);
}
