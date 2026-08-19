package com.nexus.backend.invite;

import com.nexus.backend.domain.Invite;
import com.nexus.backend.domain.Role;

import java.time.Instant;

public record InviteResponse(Long id, String email, Role role, Instant expiresAt, Instant createdAt) {

    public static InviteResponse from(Invite invite) {
        return new InviteResponse(invite.getId(), invite.getEmail(), invite.getRole(), invite.getExpiresAt(), invite.getCreatedAt());
    }
}
