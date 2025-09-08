package com.blewandowicz.library_system.auth.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.blewandowicz.library_system.auth.exception.InvalidJWTException;
import com.blewandowicz.library_system.common.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtUtils {
    private final JwtProperties jwtProperties;

    /**
     * Builds a SecretKey for HMAC signing from the Base64-encoded secret in jwtProperties.
     *
     * Decodes the configured Base64 secret and returns a key suitable for HS256 signing.
     *
     * @return SecretKey derived from the Base64-encoded secret
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Parses and verifies a signed JWT and returns its claims payload.
     *
     * Attempts to parse the provided JWT using the service's signing key. If the token
     * is malformed, has an invalid signature, or cannot be parsed, an InvalidJWTException
     * is thrown.
     *
     * @param token the compact signed JWT string
     * @return the parsed Claims payload from the token
     * @throws InvalidJWTException if the token is invalid or cannot be parsed
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new InvalidJWTException("Invalid JWT token");
        }
    }

    /**
     * Extracts a single value from the JWT's claims by applying the provided resolver.
     *
     * @param token the JWT string to parse
     * @param claimsResolver function that maps the parsed Claims to the desired value
     * @param <T> the type of the extracted claim value
     * @return the value produced by applying {@code claimsResolver} to the token's claims
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Returns the expiration instant of the given JWT.
     *
     * @param token the JWT string
     * @return the token's expiration date from its claims
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts the JWT subject (used as the username) from the given token.
     *
     * @param token the compact JWT string
     * @return the token's subject (username), or {@code null} if the subject claim is absent
     * @throws InvalidJWTException if the token is invalid or cannot be parsed (including expired tokens)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Checks whether the given JWT is expired.
     *
     * @param token the JWT string to check
     * @return {@code true} if the token's expiration time is before the current time, otherwise {@code false}
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates that the given JWT is for the provided user and is not expired.
     *
     * Checks that the token's subject (username) equals {@code userDetails.getUsername()}
     * and that the token's expiration date has not passed.
     *
     * @param token the JWT string to validate
     * @param userDetails the user details whose username must match the token's subject
     * @return {@code true} if the token's subject matches the user and the token is not expired; {@code false} otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Extracts the JWT subject (username), returning it even if the token is expired.
     *
     * <p>Parses the provided JWT and returns its subject. If the token is expired, the subject
     * is retrieved from the ExpiredJwtException's claims. For any other JWT parsing/validation
     * error, an {@code InvalidJWTException} is thrown.
     *
     * @param token the JWT string (may be expired)
     * @return the subject (username) contained in the token
     * @throws InvalidJWTException if the token is invalid for reasons other than expiration
     */
    public String extractUsernameAlsoIfExpired(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            throw new InvalidJWTException("Invalid JWT token");
        }

    }

    /**
     * Generates a signed JWT for the given user with optional extra claims.
     *
     * The token's subject is set to the user's username, issued-at is set to the current time,
     * and expiration is current time plus the value from jwtProperties.getExpiration().
     *
     * @param extraClaims additional claims to include in the token; may be empty
     * @param userDetails the authenticated user's details (username is used as the token subject)
     * @return a compact serialized JWT string signed with the service's HMAC key
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generates a JWT for the given user with no extra claims.
     *
     * Delegates to {@link #generateToken(Map, UserDetails)} with an empty claims map and returns the compact JWT string.
     *
     * @param userDetails the authenticated user's details used as the token subject
     * @return a signed JWT string containing the user's username as subject
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Extracts a Bearer JWT from the request's Authorization header.
     *
     * Returns the token string when the Authorization header is present and starts with "Bearer ",
     * otherwise returns null.
     *
     * @param request the HTTP servlet request potentially containing an `Authorization: Bearer <token>` header
     * @return the JWT string without the "Bearer " prefix, or {@code null} if no Bearer token is present
     */
    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

}
