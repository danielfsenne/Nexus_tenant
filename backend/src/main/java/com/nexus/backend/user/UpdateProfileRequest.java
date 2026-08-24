package com.nexus.backend.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(@NotBlank String name) {
}
