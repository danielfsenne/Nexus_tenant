package com.nexus.backend.auth;

public record AuthResponse(
        String token,
        Long tenantId,
        String role
) {
}
