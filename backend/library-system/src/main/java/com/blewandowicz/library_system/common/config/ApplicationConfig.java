package com.blewandowicz.library_system.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.blewandowicz.library_system.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;

    /**
     * Provides a UserDetailsService that loads a user by email for authentication.
     *
     * The returned service treats the supplied username as an email address and looks it up
     * via the injected UserRepository. If no user is found, a UsernameNotFoundException is thrown.
     *
     * @return a UserDetailsService which returns the user for the given email (username)
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the user does not exist
     */

    @Bean
    public UserDetailsService userDetailsService() {
        return (String username) -> userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("Nie znaleziono użytkownika w trakcie uwierzytelniania"));
    }

    /**
     * Provides a PasswordEncoder that uses Argon2 for hashing passwords.
     *
     * This encoder is configured with the following Argon2 parameters:
     * salt length = 16 bytes, hash length = 32 bytes, parallelism = 1,
     * memory = 60000 KB, iterations = 10.
     *
     * @return a configured Argon2 PasswordEncoder suitable for secure password hashing
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
    }

    /**
     * Exposes the application's AuthenticationManager as a Spring bean by delegating to
     * the provided AuthenticationConfiguration.
     *
     * @return the resolved AuthenticationManager
     * @throws Exception if the AuthenticationManager cannot be obtained from the configuration
     */

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Spring bean that provides a DaoAuthenticationProvider wired for application authentication.
     *
     * The provider uses the configured UserDetailsService to load users by username (email) and
     * the application's PasswordEncoder for password verification.
     *
     * @return a configured AuthenticationProvider (DaoAuthenticationProvider)
     */

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
