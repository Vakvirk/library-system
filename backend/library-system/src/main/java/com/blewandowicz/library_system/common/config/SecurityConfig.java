package com.blewandowicz.library_system.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import com.blewandowicz.library_system.common.filters.JWTAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final JWTAuthFilter jwtAuthFilter;
        private final AuthenticationProvider authenticationProvider;

        // TODO: csrf

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // 1. Nagłowki: referrerPolicy - ile info o nadawcy widać przy przkierowaniu
                // 2. AuthRequest: ogólnodostępne endpointy (requestMatchers().permitAll), inne
                // do uwierzytelniania (anyRequest().authenticated())
                // 3. Sesja: bez sesji, STATELESS
                // 4. AuthProvider ze springa
                // 5. Ddodanie customowego filtra jwt przed standardowym uwierzytelnianiem

                http
                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin())
                                                .referrerPolicy(referrer -> referrer
                                                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                                .authorizeHttpRequests(
                                                auth -> auth.requestMatchers("/api/auth/**", "/h2-console/**")
                                                                .permitAll()
                                                                .anyRequest().authenticated())
                                // auth -> auth.anyRequest().permitAll())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"error\": \"Authentication is required to connect to this resource\"}");
                                                })
                                                .accessDeniedHandler((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("{\"error\": \"Access denied\"}");
                                                }))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}
