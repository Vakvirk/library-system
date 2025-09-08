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

    /**
     * Authenticates an HTTP request using a JWT from the `Authorization: Bearer ...` header
     * and, when the token is valid, populates the Spring Security context with an authenticated
     * principal for the remainder of the request.
     *
     * <p>Behavior:
     * - Requests whose servlet path starts with {@code /api/auth} are left untouched and proceed
     *   down the filter chain.
     * - If the {@code Authorization} header is absent or does not start with {@code "Bearer "},
     *   the filter does not attempt authentication and continues the chain.
     * - Otherwise the JWT is extracted, the username is resolved via {@code jwtUtils}, and if
     *   there is no existing authentication in the {@code SecurityContext}, the user details are
     *   loaded and the token is validated. When valid, a {@code UsernamePasswordAuthenticationToken}
     *   with the user's authorities is stored in the {@code SecurityContextHolder}.
     *
     * <p>If the token is missing or invalid, no authentication is set and the request proceeds normally.
     *
     * @param request     the current HTTP request
     * @param response    the current HTTP response
     * @param filterChain the filter chain to continue when this filter is done
     * @throws ServletException if an error occurs during request processing
     * @throws IOException      if an I/O error occurs during request processing
     */
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
