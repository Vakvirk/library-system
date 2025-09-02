package com.blewandowicz.library_system.common.filters;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.blewandowicz.library_system.auth.jwt.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {
    final JwtUtils jwtUtils;
    final UserDetailsService userDetailsService;

    // Logika filtra JWT
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Jeżeli call do autha, nie próbuje uwierzytelniać

        String path = request.getServletPath();
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Pobiera nagłówek z żądania

        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String username;

        // Jeżeli zły nagłówek nie próbuje uwierzytelniać

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // wyciąganie jwt i usernamea z niego

        jwtToken = authHeader.substring(7);
        username = jwtUtils.extractUsername(jwtToken);

        // czy token ma subject i czy nie został już uwierzytelniony

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Pobieranie info o userze

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Czy user jest valid

            if (jwtUtils.isTokenValid(jwtToken, userDetails)) {

                // Tworzy i zapisuje obiekt uwierzytelnienia

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);

    }
}
