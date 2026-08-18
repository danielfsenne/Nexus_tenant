package com.nexus.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String companyName,
        @NotBlank String adminName,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String password
) {
}
