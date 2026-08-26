package com.nexus.backend.auth;

public record AuthResponse(
        String token,
        String refreshToken,
        Long tenantId,
        String role
) {
}
