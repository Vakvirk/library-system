package com.blewandowicz.library_system.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.blewandowicz.library_system.auth.dto.AuthenticationRequest;
import com.blewandowicz.library_system.auth.dto.AuthenticationResponse;
import com.blewandowicz.library_system.auth.dto.RegisterRequest;
import com.blewandowicz.library_system.auth.jwt.JwtUtils;
import com.blewandowicz.library_system.user.User;
import com.blewandowicz.library_system.user.UserMapper;
import com.blewandowicz.library_system.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    @Transactional
    public ResponseEntity<String> register(RegisterRequest request, HttpServletResponse response) {
        String hashedPassword = passwordEncoder.encode(request.password());
        RegisterRequest withHashedPassword = new RegisterRequest(
                request.name(),
                request.lastName(),
                request.email(),
                hashedPassword);
        User user = UserMapper.INSTANCE.registerToUser(withHashedPassword);
        userRepository.save(user);
        return ResponseEntity.ok("Pomyślnie zarejestrowano.");
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request,
            HttpServletResponse response) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono uzytkownika po uwierzytelnieniu"));
        var jwtToken = jwtUtils.generateToken(user);

        AuthenticationResponse authResponse = AuthenticationResponse.builder().jwt(jwtToken).build();

        return authResponse;

    }
}
