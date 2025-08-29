package com.blewandowicz.library_system.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootTest
public class JwtUtilsTest {
    @Autowired
    private JwtUtils jwtUtils;

    private static final Logger log = LogManager.getLogger();

    @Test
    void shouldGenerateValidToken() {
        UserDetails user = User.withUsername("testuser")
                .password("password")
                .roles("client")
                .build();

        String token = jwtUtils.generateToken(new HashMap<>(), user);

        assertNotNull(token);
        assertEquals(token.split("\\.").length, 3);

        log.info("Generated token: " + token);

    }
}
