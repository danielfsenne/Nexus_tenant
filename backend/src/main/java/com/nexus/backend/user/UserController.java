package com.nexus.backend.user;

import com.nexus.backend.auth.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.findMe();
    }

    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateMe(request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(request));
    }

    @PostMapping("/me/resend-verification")
    public ResponseEntity<Void> resendVerification() {
        userService.resendVerification();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/logout-all")
    public ResponseEntity<Void> logoutAllSessions() {
        userService.logoutAllSessions();
        return ResponseEntity.ok().build();
    }
}
