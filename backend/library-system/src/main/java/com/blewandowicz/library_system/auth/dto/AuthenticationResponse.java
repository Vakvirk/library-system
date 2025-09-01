package com.blewandowicz.library_system.auth.dto;

import lombok.Builder;

@Builder
public record AuthenticationResponse(String jwt) {

}
