package com.nexus.backend.auth;

import com.nexus.backend.common.ConflictException;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.PasswordResetToken;
import com.nexus.backend.domain.Plan;
import com.nexus.backend.domain.Role;
import com.nexus.backend.domain.Tenant;
import com.nexus.backend.domain.User;
import com.nexus.backend.repository.PasswordResetTokenRepository;
import com.nexus.backend.repository.TenantRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String frontendUrl;
    private final long passwordResetExpirationHours;

    public AuthService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${nexus.frontend-url}") String frontendUrl,
            @Value("${nexus.password-reset-expiration-hours}") long passwordResetExpirationHours
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
        this.passwordResetExpirationHours = passwordResetExpirationHours;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Tenant tenant = tenantRepository.save(
                Tenant.builder()
                        .name(request.companyName())
                        .plan(Plan.FREE)
                        .build()
        );

        User admin = userRepository.save(
                User.builder()
                        .name(request.adminName())
                        .email(request.email())
                        .password(passwordEncoder.encode(request.password()))
                        .role(Role.ADMIN)
                        .tenantId(tenant.getId())
                        .build()
        );

        String token = jwtService.generateToken(admin);
        return new AuthResponse(token, tenant.getId(), admin.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getTenantId(), user.getRole().name());
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> user = userRepository.findByEmail(request.email());

        if (user.isEmpty()) {
            // Não revela se o e-mail existe ou não.
            return;
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.save(
                PasswordResetToken.builder()
                        .userId(user.get().getId())
                        .token(UUID.randomUUID().toString())
                        .expiresAt(Instant.now().plusSeconds(passwordResetExpirationHours * 60 * 60))
                        .build()
        );

        String resetLink = frontendUrl + "/redefinir-senha?token=" + resetToken.getToken();
        log.info("Redefinição de senha solicitada para {}. Link: {}", user.get().getEmail(), resetLink);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new ResourceNotFoundException("Token de redefinição inválido"));

        if (resetToken.isUsed()) {
            throw new ConflictException("Este link de redefinição já foi utilizado.");
        }
        if (resetToken.isExpired()) {
            throw new ConflictException("Este link de redefinição expirou.");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);
    }
}
