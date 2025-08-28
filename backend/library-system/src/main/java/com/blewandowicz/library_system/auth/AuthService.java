package com.blewandowicz.library_system.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.blewandowicz.library_system.user.User;
import com.blewandowicz.library_system.user.UserMapper;
import com.blewandowicz.library_system.user.UserRepository;
import com.blewandowicz.library_system.user.dto.UserCreateDTO;
import com.blewandowicz.library_system.user.dto.UserFetchDTO;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    public ResponseEntity<String> createUser(UserCreateDTO dto) {
        User user = UserMapper.INSTANCE.userCreateToUser(dto);
        userRepository.save(user);
        return ResponseEntity.ok("Pomyślnie dodano użytkownika.");
    }

    public ResponseEntity<UserFetchDTO> fetchUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono uzytkownika z emailem: " + email));
        UserFetchDTO dto = UserMapper.INSTANCE.userToUserFetch(user);

        return ResponseEntity.ok(dto);
    }
}
