package com.nexus.backend.user;

import com.nexus.backend.auth.AuthResponse;
import com.nexus.backend.auth.AuthService;
import com.nexus.backend.common.ConflictException;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.User;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.CurrentUserContext;
import com.nexus.backend.security.TenantContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAllByTenantId(TenantContext.get()).stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findMe() {
        return UserResponse.from(currentUser());
    }

    public UserResponse updateMe(UpdateProfileRequest request) {
        User user = currentUser();
        user.setName(request.name());
        return UserResponse.from(userRepository.save(user));
    }

    public AuthResponse changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Senha atual incorreta");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Uma senha comprometida invalida qualquer sessão aberta com a senha
        // antiga: revoga todos os refresh tokens ativos do usuário, e emite
        // um par novo para a sessão atual continuar funcionando sem precisar
        // logar de novo.
        authService.logoutAllSessions(user.getId());
        return authService.buildAuthResponse(user);
    }

    public void logoutAllSessions() {
        authService.logoutAllSessions(currentUser().getId());
    }

    public void resendVerification() {
        User user = currentUser();
        if (user.isEmailVerified()) {
            throw new ConflictException("Este e-mail já foi verificado.");
        }
        authService.sendVerificationEmail(user);
    }

    private User currentUser() {
        CurrentUserContext.CurrentUser current = CurrentUserContext.get();
        return userRepository.findByIdAndTenantId(current.id(), TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
