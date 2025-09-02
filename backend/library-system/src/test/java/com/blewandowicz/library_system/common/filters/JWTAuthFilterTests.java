package com.blewandowicz.library_system.common.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;

import com.blewandowicz.library_system.auth.jwt.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public class JWTAuthFilterTests {
    private JwtUtils jwtUtils;
    private UserDetailsService userDetailsService;
    private JWTAuthFilter jwtAuthFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        jwtUtils = Mockito.mock(JwtUtils.class);
        userDetailsService = Mockito.mock(UserDetailsService.class);
        jwtAuthFilter = new JWTAuthFilter(jwtUtils, userDetailsService);

        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotAuthenticate_whenAuthPath() throws Exception {
        when(request.getServletPath()).thenReturn("/api/auth");
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader("Authorization");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticate_whenNoAuthorizationHeader() throws Exception {
        when(request.getServletPath()).thenReturn("api/books");
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticate_whenInvalidAuthorizationHeader() throws Exception {
        when(request.getServletPath()).thenReturn("api/books");
        when(request.getHeader("Authorization")).thenReturn("Invalid authorization header");
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotAuthenticate_whenNoUsername() throws Exception {
        String token = "valid.jwt.token";
        when(request.getServletPath()).thenReturn("api/books");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenReturn(null);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, times(1)).getHeader("Authorization");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticate_whenValidToken() throws Exception {
        String token = "valid.jwt.token";
        String username = "testUser";

        when(request.getServletPath()).thenReturn("api/books");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenReturn(username);

        UserDetails userDetails = User.withUsername(username).password("password").authorities(List.of()).build();

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtils.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username,
                ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());

    }

    @Test
    void shouldNotAuthenticate_whenInvalidToken() throws Exception {
        String token = "invalid.jwt.token";
        String username = "testUser";

        when(request.getServletPath()).thenReturn("api/books");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenReturn(username);

        UserDetails userDetails = User.withUsername(username).password("password").authorities(List.of()).build();

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtils.isTokenValid(token, userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

}
