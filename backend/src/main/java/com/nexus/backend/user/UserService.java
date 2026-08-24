package com.nexus.backend.user;

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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Senha atual incorreta");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User currentUser() {
        CurrentUserContext.CurrentUser current = CurrentUserContext.get();
        return userRepository.findByIdAndTenantId(current.id(), TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
