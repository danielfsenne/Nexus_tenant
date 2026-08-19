package com.nexus.backend.invite;

import com.nexus.backend.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteRequest(
        @Email @NotBlank String email,
        @NotNull Role role
) {
}
