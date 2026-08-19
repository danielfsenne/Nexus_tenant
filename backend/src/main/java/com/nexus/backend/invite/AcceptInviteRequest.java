package com.nexus.backend.invite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInviteRequest(
        @NotBlank String token,
        @NotBlank String name,
        @NotBlank @Size(min = 6) String password
) {
}
