package com.blewandowicz.library_system.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blewandowicz.library_system.user.dto.UserCreateDTO;
import com.blewandowicz.library_system.user.dto.UserFetchDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/{email}")
    public ResponseEntity<UserFetchDTO> fetchUser(@PathVariable String email) {
        return authService.fetchUser(email);
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserCreateDTO data) {
        return authService.createUser(data);
    }
}
