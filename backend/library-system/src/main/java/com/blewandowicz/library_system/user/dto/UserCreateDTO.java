package com.blewandowicz.library_system.user.dto;

public record UserCreateDTO(String name, String lastName, String email, String passwordHash) {

}
