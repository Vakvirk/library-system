package com.blewandowicz.library_system.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blewandowicz.library_system.auth.dto.AuthenticationRequest;
import com.blewandowicz.library_system.auth.dto.AuthenticationResponse;
import com.blewandowicz.library_system.auth.dto.RegisterRequest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.authenticate(request, response));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        return authService.register(request, response);
    }
}
