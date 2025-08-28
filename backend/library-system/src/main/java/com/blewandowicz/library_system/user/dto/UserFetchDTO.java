package com.blewandowicz.library_system.user.dto;

import java.util.UUID;

public record UserFetchDTO(UUID id, String name, String lastName, String email, String role) {

}
