package com.blewandowicz.library_system.auth.refreshToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.blewandowicz.library_system.common.config.JwtProperties;
import com.blewandowicz.library_system.user.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private RefreshToken testRefreshToken;
    private static final long REFRESH_EXPIRATION_MS = 7200000L;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("password")
                .name("User")
                .lastName("Testowy")
                .role("client")
                .build();
        testRefreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusHours(2L))
                .build();
    }

    // AAA - Arrange, Act, Assert
    // Arrange - Ustaw warunki testu
    // Act - Wykonaj testowane metody
    // Assert - Zweryfikuj wyniki

    @Nested
    @DisplayName("createRefreshToken() Tests")
    class createRefreshTokenTests {
        @Test
        @DisplayName("Should create refresh token with valid user")
        void createRefreshToken_ShouldCreateWithValidUser() {

            // Arrange

            when(jwtProperties.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION_MS);
            when(refreshTokenRepository.deleteByUser(testUser)).thenReturn(1);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

            // Act

            RefreshToken result = refreshTokenService.createRefreshToken(testUser);

            // Assert

            assertNotNull(result, "Created refresh token should not be null");
            assertEquals(testUser, result.getUser(), "Token should be associated with the correct user");
            assertNotNull(result.getToken(), "Token string should not be null");
            assertTrue(result.getExpiryDate().isAfter(LocalDateTime.now()), "Token expiry should be in the future");

            // Weryfikacja wykonanych interakcji (ważne przy operacjach na bazie danych)

            verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
            verify(refreshTokenRepository, times(1)).flush();
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
            verify(jwtProperties, times(1)).getRefreshExpiration();
        }

        @Test
        @DisplayName("Should delete old token before creating a new one")
        void createRefreshToken_ShouldDeleteOldBeforeCreatingNew() {

            // Arrange

            when(jwtProperties.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION_MS);
            when(refreshTokenRepository.deleteByUser(testUser)).thenReturn(1);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);
            InOrder inOrder = inOrder(refreshTokenRepository); // Sprawdanie kolejności wykonywanych operacji

            // Act

            refreshTokenService.createRefreshToken(testUser);

            // Assert
            // Sprawdza czy operacje były wykonane w kolejności takiej jak kolejnośc
            // wywoływania InOrder
            inOrder.verify(refreshTokenRepository).deleteByUser(testUser);
            inOrder.verify(refreshTokenRepository).flush();
            inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should create token with correct expiration date")
        void createRefreshToken_ShouldCreateWithCorrectValues() {

            // Arrange

            LocalDateTime beforeCall = LocalDateTime.now();
            when(jwtProperties.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION_MS);
            when(refreshTokenRepository.deleteByUser(testUser)).thenReturn(1);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);
            ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class); // Wyciąganie
                                                                                                    // wartości z
                                                                                                    // obiektów
                                                                                                    // testowych

            // Act

            refreshTokenService.createRefreshToken(testUser);
            verify(refreshTokenRepository).save(tokenCaptor.capture()); // Wyciąganie obiektu wykorzystanego w
                                                                        // weryfikowanej metodzie
            RefreshToken capturedToken = tokenCaptor.getValue(); // Wyciąganie wartości z obiektu

            // Assert

            assertEquals(testUser, capturedToken.getUser());
            assertNotNull(capturedToken.getToken());
            assertTrue(capturedToken.getToken().length() > 0);

            LocalDateTime expectedExpiry = beforeCall.plusSeconds(REFRESH_EXPIRATION_MS / 1000);
            LocalDateTime afterCall = LocalDateTime.now().plusSeconds(REFRESH_EXPIRATION_MS / 1000);
            assertTrue(capturedToken.getExpiryDate().isAfter(expectedExpiry.minusSeconds(1))
                    && capturedToken.getExpiryDate().isBefore(afterCall.plusSeconds(1)),
                    "Should generate token with correct expiration date.");
        }

        @Test
        @DisplayName("Should throw exception when User is null")
        void createRefreshToken_ShouldThrowExceptionWithNullUser() {
            // Arrange

            // Act + Assert

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> refreshTokenService.createRefreshToken(null), "Should throw IllegalArgumentException");
            assertEquals("User nie może być null.", exception.getMessage(), "Should display correct message");
        }
    }

    @Nested
    @DisplayName("verifyExpiration() Tests")
    class VerifyExpirationTests {
        @Test
        @DisplayName("Should return token when not expired")
        void verifyExpiration_ShouldReturnTokenWhenNotExpired() {
            // Arrange
            RefreshToken validToken = testRefreshToken;

            // Act
            RefreshToken result = refreshTokenService.verifyExpiration(validToken);

            // Assert
            assertEquals(validToken, result, "Should return the same token when valid");
        }

        @Test
        @DisplayName("Should throw exception when token is expired.")
        void verifyExpiration_ShouldThrowExceptionWhenExpired() {
            // Arrange
            RefreshToken invalidToken = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(LocalDateTime.now().minusHours(2L))
                    .build();

            // Act + Assert - wyjątki weryfikuje przy wywołaniu
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> refreshTokenService.verifyExpiration(invalidToken),
                    "Should throw ResponseStatusException");

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode(), "Exception should have FORBIDDEN status.");
            assertEquals("Refresh token wygasł.", exception.getReason(),
                    "Exception should have correct error message.");
        }

        @Test
        @DisplayName("Should throw exception when token expires exactly now.")
        void verifyExpiration_ShouldThrowExceptionWhenExpiresNow() {
            // Arrange
            RefreshToken expiresNow = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(LocalDateTime.now().minusNanos(1L))
                    .build();

            // Act + Assert - wyjątki weryfikuje przy wywołaniu
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> refreshTokenService.verifyExpiration(expiresNow),
                    "Should throw ResponseStatusException");

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode(), "Exception should have FORBIDDEN status.");
            assertEquals("Refresh token wygasł.", exception.getReason(),
                    "Exception should have correct error message.");
        }

        @Test
        @DisplayName("Should throw exception when token is null")
        void verifyExpiration_ShouldThrowExceptionWhenTokenNull() {
            // Arrange

            // Act + Assert

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> refreshTokenService.verifyExpiration(null), "Should throw IllegalArgumentException");
            assertEquals("Token nie może być null.", exception.getMessage(), "Should display correct message");
        }

        @Test
        @DisplayName("Should throw exception when expiration date is null")
        void verifyExpiration_ShouldThrowExceptionWhenExpiryDateNull() {
            RefreshToken noExpiryDate = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .user(testUser)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(null)
                    .build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> refreshTokenService.verifyExpiration(noExpiryDate), "Should throw IllegalArgumentException");
            assertEquals("Token nie ma daty wygaśniecia.", exception.getMessage(), "Should display correct message");
        }
    }

    @Nested
    @DisplayName("findToken() Tests")
    class FindTokenTests {
        @Test
        @DisplayName("Should find token when it exists.")
        void findToken_ShouldFindTokenWhenExists() {
            // Arrange
            String tokenString = "existingToken";
            when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(testRefreshToken));

            // Act
            Optional<RefreshToken> result = refreshTokenService.findToken(tokenString);

            // Assert
            assertEquals(testRefreshToken, result.get(), "Should find correct token");
            verify(refreshTokenRepository, times(1)).findByToken(tokenString);
        }

        @Test
        @DisplayName("Should return empty Optional when token not found")
        void findToken_ShouldReturnEmpptyWhenNotFound() {
            // Arrange
            String tokenString = "nonExistingToken";
            when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());

            Optional<RefreshToken> result = refreshTokenService.findToken(tokenString);

            assertFalse(result.isPresent(), "Should return empty Optional when token doesn't exist");
            verify(refreshTokenRepository, times(1)).findByToken(tokenString);
        }

        @Test
        @DisplayName("Should return empty Optional when input is null")
        void findToken_ShouldReturnEmptyWhenNull() {
            Optional<RefreshToken> result = refreshTokenService.findToken(null);

            assertEquals(Optional.empty(), result);
            verify(refreshTokenRepository, never()).findByToken(null);
        }

        @Test
        @DisplayName("Should return empty Optional when input is blank")
        void findToken_ShouldReturnEmptyWhenBlank() {
            Optional<RefreshToken> result = refreshTokenService.findToken(" ");

            assertEquals(Optional.empty(), result);
            verify(refreshTokenRepository, never()).findByToken(" ");
        }
    }

    @Nested
    @DisplayName("deleteByUser() Tests")
    class DeleteByUserTests {
        @Test
        @DisplayName("Should delete token if valid user is given")
        void deleteByUser_ShouldDeleteToken() {
            when(refreshTokenRepository.deleteByUser(testUser)).thenReturn(1);

            Integer result = refreshTokenService.deleteByUser(testUser);

            assertEquals(1, result, "Should delete if user valid");
            verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
        }

        @Test
        @DisplayName("Should not delete when invalid user")
        void deleteByUser_ShouldNotDeleteWhenInvalidUser() {
            when(refreshTokenRepository.deleteByUser(testUser)).thenReturn(0);

            Integer result = refreshTokenService.deleteByUser(testUser);

            assertEquals(0, result, "Should not delete when user not valid");
            verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user is null")
        void deleteByUser_ShouldThrowExceptionWhenUserNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> refreshTokenService.deleteByUser(null), "Should throw IllegalArgumentException.");

            assertEquals("User nie może być null.", exception.getMessage(), "Should display correct message");
        }
    }

}
