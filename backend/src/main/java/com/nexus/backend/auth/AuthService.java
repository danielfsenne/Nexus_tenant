package com.nexus.backend.auth;

import com.nexus.backend.common.ConflictException;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.EmailVerificationToken;
import com.nexus.backend.domain.PasswordResetToken;
import com.nexus.backend.domain.Plan;
import com.nexus.backend.domain.Role;
import com.nexus.backend.domain.Tenant;
import com.nexus.backend.domain.User;
import com.nexus.backend.mail.MailService;
import com.nexus.backend.repository.EmailVerificationTokenRepository;
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
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final String frontendUrl;
    private final long passwordResetExpirationHours;
    private final long emailVerificationExpirationHours;

    public AuthService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            MailService mailService,
            @Value("${nexus.frontend-url}") String frontendUrl,
            @Value("${nexus.password-reset-expiration-hours}") long passwordResetExpirationHours,
            @Value("${nexus.email-verification-expiration-hours}") long emailVerificationExpirationHours
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.frontendUrl = frontendUrl;
        this.passwordResetExpirationHours = passwordResetExpirationHours;
        this.emailVerificationExpirationHours = emailVerificationExpirationHours;
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

        sendVerificationEmail(admin);

        String token = jwtService.generateToken(admin);
        return new AuthResponse(token, tenant.getId(), admin.getRole().name());
    }

    public void sendVerificationEmail(User user) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.save(
                EmailVerificationToken.builder()
                        .userId(user.getId())
                        .token(UUID.randomUUID().toString())
                        .expiresAt(Instant.now().plusSeconds(emailVerificationExpirationHours * 60 * 60))
                        .build()
        );

        String verifyLink = frontendUrl + "/verificar-email?token=" + verificationToken.getToken();
        log.info("Verificação de e-mail solicitada para {}. Link: {}", user.getEmail(), verifyLink);

        mailService.send(
                user.getEmail(),
                "Confirme seu e-mail - Nexus",
                "Falta pouco para começar a usar o Nexus.\n\n"
                        + "Clique no link abaixo para confirmar seu e-mail:\n" + verifyLink + "\n\n"
                        + "Este link expira em " + emailVerificationExpirationHours + " horas."
        );
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new ResourceNotFoundException("Token de verificação inválido"));

        if (verificationToken.isUsed()) {
            throw new ConflictException("Este link de verificação já foi utilizado.");
        }
        if (verificationToken.isExpired()) {
            throw new ConflictException("Este link de verificação expirou.");
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(verificationToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findFirstByEmailOrderByIdAsc(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getTenantId(), user.getRole().name());
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> user = userRepository.findFirstByEmailOrderByIdAsc(request.email());

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

        mailService.send(
                user.get().getEmail(),
                "Redefinição de senha - Nexus",
                "Recebemos uma solicitação para redefinir sua senha no Nexus.\n\n"
                        + "Clique no link abaixo para criar uma nova senha:\n" + resetLink + "\n\n"
                        + "Este link expira em " + passwordResetExpirationHours + " horas. "
                        + "Se você não solicitou isso, pode ignorar este e-mail."
        );
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
