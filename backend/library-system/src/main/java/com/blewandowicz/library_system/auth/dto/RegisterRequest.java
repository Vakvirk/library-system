package com.blewandowicz.library_system.auth.dto;

public record RegisterRequest(String name, String lastName, String email, String password) {

}
