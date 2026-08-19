package com.nexus.backend.invite;

import com.nexus.backend.auth.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class InviteController {

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping("/invites")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InviteResponse> create(@Valid @RequestBody InviteRequest request) {
        return ResponseEntity.ok(inviteService.create(request));
    }

    @GetMapping("/invites")
    @PreAuthorize("hasRole('ADMIN')")
    public List<InviteResponse> findPending() {
        return inviteService.findPending();
    }

    @PostMapping("/invites/accept")
    public ResponseEntity<AuthResponse> accept(@Valid @RequestBody AcceptInviteRequest request) {
        return ResponseEntity.ok(inviteService.accept(request));
    }
}
