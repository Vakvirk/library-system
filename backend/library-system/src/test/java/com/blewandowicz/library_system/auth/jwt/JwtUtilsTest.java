package com.blewandowicz.library_system.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import com.blewandowicz.library_system.common.config.JwtProperties;

import io.jsonwebtoken.Jwts;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public class JwtUtilsTest {
    private JwtProperties jwtProperties;
    private JwtUtils jwtUtils;
    private String secret = "e6f1567c26387a1d0a1940bd950d35742741ff97efcf1e54bb78ddc45e408981";
    // private static final Logger log = LogManager.getLogger();

    @BeforeEach
    void setup() {
        jwtProperties = Mockito.mock(JwtProperties.class);
        Mockito.when(jwtProperties.getSecret()).thenReturn(secret);
        Mockito.when(jwtProperties.getExpiration()).thenReturn((long) (15 * 60 * 1000));
        jwtUtils = new JwtUtils(jwtProperties);
    }

    private UserDetails buildUser(String username) {
        UserDetails user = Mockito.mock(UserDetails.class);
        Mockito.when(user.getUsername()).thenReturn(username);
        return user;
    }

    private String createExpiredToken(String username) {
        SecretKey key = Jwts.SIG.HS256.key().build();
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
        key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 5000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Test
    void shouldGenerateValidToken() {
        UserDetails user = buildUser("testuser");
        String token = jwtUtils.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtUtils.isTokenValid(token, user));
    }

    @Test
    void shouldDetectExpiredToken() {
        UserDetails user = buildUser("expireduser");
        String expiredToken = createExpiredToken(user.getUsername());

        assertThrows(RuntimeException.class, () -> jwtUtils.isTokenValid(expiredToken, user));
        // assertFalse(jwtUtils.isTokenValid(expiredToken, user));
    }

    @Test
    void shouldDetectInvalidUser() {
        UserDetails user = buildUser("user1");
        UserDetails fakeUser = buildUser("user2");
        String token = jwtUtils.generateToken(user);

        assertFalse(jwtUtils.isTokenValid(token, fakeUser));
    }

    @Test
    void shouldExtractUsernameAlsoIfExpired_valid() {
        UserDetails user = buildUser("user1");
        String token = jwtUtils.generateToken(user);

        String extracted = jwtUtils.extractUsernameAlsoIfExpired(token);
        assertEquals(user.getUsername(), extracted);
    }

    @Test
    void shouldExtractUsernameAlsoIfExpired_expired() {
        UserDetails user = buildUser("user1");
        String token = createExpiredToken(user.getUsername());

        String extracted = jwtUtils.extractUsernameAlsoIfExpired(token);
        assertEquals(user.getUsername(), extracted);
    }
}
