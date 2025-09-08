package com.blewandowicz.library_system.auth.refreshToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.blewandowicz.library_system.user.User;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RefreshTokenRepository Integration Tests")
public class refreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser1;
    private User testUser2;
    private RefreshToken validToken1;
    private RefreshToken validToken2;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .name("name1")
                .lastName("lastName1")
                .email("test1@example.com")
                .passwordHash("password123")
                .build();

        testUser2 = User.builder()
                .name("name2")
                .lastName("lastName2")
                .email("test2@example.com")
                .passwordHash("password456")
                .build();

        validToken1 = RefreshToken.builder()
                .token("valid-token-1-" + UUID.randomUUID())
                .expiryDate(LocalDateTime.now().plusHours(2L))
                .build();

        validToken2 = RefreshToken.builder()
                .token("valid-token-2-" + UUID.randomUUID())
                .expiryDate(LocalDateTime.now().plusHours(2L))
                .build();
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperationTests {
        @Test
        @DisplayName("Should save refresh token with valid user relationship")
        void save_WithValidUserRelationship_ShouldPersistSuccessfully() {
            // Arrange
            User savedUser = entityManager.persistAndFlush(testUser1);
            validToken1.setUser(savedUser);

            // Act
            RefreshToken savedToken = refreshTokenRepository.save(validToken1);

            // Assert
            assertNotNull(savedToken, "Should save token object");
            assertNotNull(savedToken.getId(), "Saved token should have id");
            assertNotNull(savedToken.getToken(), "Saved token should have value");
            assertNotNull(savedToken.getUser(), "Saved token should have user");
            assertNotNull(savedToken.getExpiryDate(), "Saved token should have expiry date");

            // Czyszczenie cachea
            entityManager.flush();
            entityManager.clear();

            RefreshToken reloadedToken = entityManager.find(RefreshToken.class, savedToken.getId());
            assertNotNull(reloadedToken, "Should find token after clearing cache");
            assertEquals(reloadedToken.getToken(), savedToken.getToken(),
                    "Found token should have same value as saved token");

        }

        @Test
        @DisplayName("Should find refresh token by ID")
        void findById_ShouldReturnTokenWithExistingToken() {
            // Arrange
            User savedUser = entityManager.persistAndFlush((testUser1));
            validToken1.setUser(savedUser);
            RefreshToken savedToken = entityManager.persistAndFlush(validToken1);

            // Act
            Optional<RefreshToken> foundToken = refreshTokenRepository.findById(savedToken.getId());

            // Assert
            assertTrue(foundToken.isPresent(), "Should find token");
            assertEquals(savedToken.getId(), foundToken.get().getId(),
                    "Found token should have the same Id as saved Token");
            assertEquals(savedToken.getUser(), foundToken.get().getUser(),
                    "Found token should have the same User as saved Token");
            assertEquals(savedToken.getToken(), foundToken.get().getToken(),
                    "Found token should have the same Id as saved Token");

        }

        @Test
        @DisplayName("Should return empty token when not found by ID")
        void findById_ShouldReturnEmptyWithWithNonExistentId() {
            Optional<RefreshToken> foundToken = refreshTokenRepository.findById(UUID.randomUUID());

            assertFalse(foundToken.isPresent(), "Should return empty Observable.");
        }

        @Test
        @DisplayName("Should retrive all refresh tokens")
        void findAll_ShouldReturnAllTokens() {
            User savedUser1 = entityManager.persistAndFlush(testUser1);
            User savedUser2 = entityManager.persistAndFlush(testUser2);

            validToken1.setUser(savedUser1);
            validToken2.setUser(savedUser2);

            entityManager.persistAndFlush(validToken1);
            entityManager.persistAndFlush(validToken2);

            List<RefreshToken> allTokens = refreshTokenRepository.findAll();

            assertThat(allTokens).as("List should have size 2").hasSize(2);
            assertThat(allTokens).as("List should contain savedToken1 and savedToken2")
                    .containsExactlyInAnyOrder(validToken1, validToken2);

        }

        @Test
        @DisplayName("Should delete refesh token by ID")
        void deleteById_ShouldRemoveFromDatabaseWithValidID() {
            User savedUser = entityManager.persistAndFlush(testUser1);
            validToken1.setUser(savedUser);
            RefreshToken savedToken = entityManager.persistAndFlush(validToken1);
            UUID id = savedToken.getId();

            refreshTokenRepository.deleteById(id);
            entityManager.flush();

            Optional<RefreshToken> deletedToken = refreshTokenRepository.findById(id);
            assertThat(deletedToken).as("Should return empty Optional").isEmpty();
        }

    }

    @Nested
    @DisplayName("findByToken() Tests")
    class FindByTokenTests {
        @Test
        @DisplayName("Should find token when given valid token string")
        void findByToken_ShouldReturnTokenWithValidTokenString() {
            User user = entityManager.persistAndFlush(testUser1);
            validToken1.setUser(user);
            entityManager.persistAndFlush(validToken1);

            Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken(validToken1.getToken());

            assertThat(foundToken).as("Should find token").isPresent();
            assertThat(foundToken.get().getToken()).as("Found token should have same token string as valid token")
                    .isEqualTo(validToken1.getToken());
            assertThat(foundToken.get().getExpiryDate()).as("Found token should have same expiry date as valid token")
                    .isEqualTo(validToken1.getExpiryDate());
            assertThat(foundToken.get().getUser()).as("Found token should have same user as valid token")
                    .isEqualTo(validToken1.getUser());

        }

        @Test
        @DisplayName("Should return empty when token not found")
        void findByToken_ShouldReturnEmptyWhenNotFound() {
            Optional<RefreshToken> nonExistentToken = refreshTokenRepository.findByToken("non-existent-token");

            assertThat(nonExistentToken).as("Should return empty Optional when token does not exist").isEmpty();
        }

        @Test
        @DisplayName("Should handle edge cases")
        void findByToken_ShouldHandleEdgeCases() {
            Optional<RefreshToken> emptyResult = refreshTokenRepository.findByToken("");
            assertThat(emptyResult).as("Should return empty Optional when token string is blank").isEmpty();

            Optional<RefreshToken> nullResult = refreshTokenRepository.findByToken(null);
            assertThat(nullResult).as("Should return empty Optional when token string is null").isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByUser() Tests")
    class deleteByUserTests {
        @Test
        @DisplayName("Should delete one row when given valid user")
        void deleteByUser_ShouldDeleteOneWhenValidUser() {
            User user = entityManager.persistAndFlush(testUser1);
            validToken1.setUser(user);
            entityManager.persistAndFlush(validToken1);

            Integer result = refreshTokenRepository.deleteByUser(user);

            assertThat(result).as("Should delete one token when given valid user").isEqualTo(1);
        }

        @Test
        @DisplayName("Should delete zero rows when given user without token")
        void deleteByUser_ShouldDeleteZeroWhenNonExistentToken() {
            User user = entityManager.persistAndFlush(testUser1);

            Integer result = refreshTokenRepository.deleteByUser(user);

            assertThat(result).as("Should delete zero tokens when given user without token").isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle edge cases")
        void deleteByUser_ShouldHandleEdgeCases() {
            Integer result = refreshTokenRepository.deleteByUser(null);

            assertThat(result).as("Should return zero when given null user").isEqualTo(0);
        }

    }
}