package com.nexus.backend.invite;

import com.nexus.backend.auth.AuthResponse;
import com.nexus.backend.common.ConflictException;
import com.nexus.backend.common.PlanLimitService;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.Invite;
import com.nexus.backend.domain.User;
import com.nexus.backend.repository.InviteRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtService;
import com.nexus.backend.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InviteService {

    private static final Logger log = LoggerFactory.getLogger(InviteService.class);

    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final PlanLimitService planLimitService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String frontendUrl;
    private final long inviteExpirationDays;

    public InviteService(
            InviteRepository inviteRepository,
            UserRepository userRepository,
            PlanLimitService planLimitService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${nexus.frontend-url}") String frontendUrl,
            @Value("${nexus.invite-expiration-days}") long inviteExpirationDays
    ) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.planLimitService = planLimitService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
        this.inviteExpirationDays = inviteExpirationDays;
    }

    public InviteResponse create(InviteRequest request) {
        Long tenantId = TenantContext.get();

        if (userRepository.existsByEmailAndTenantId(request.email(), tenantId)) {
            throw new ConflictException("Já existe um usuário com este e-mail nesta empresa.");
        }
        if (inviteRepository.existsByEmailAndTenantIdAndAcceptedAtIsNull(request.email(), tenantId)) {
            throw new ConflictException("Já existe um convite pendente para este e-mail.");
        }

        planLimitService.assertCanCreateUser(tenantId, userRepository.countByTenantId(tenantId));

        Invite invite = inviteRepository.save(
                Invite.builder()
                        .email(request.email())
                        .role(request.role())
                        .tenantId(tenantId)
                        .token(UUID.randomUUID().toString())
                        .expiresAt(Instant.now().plusSeconds(inviteExpirationDays * 24 * 60 * 60))
                        .build()
        );

        String acceptLink = frontendUrl + "/aceitar-convite?token=" + invite.getToken();
        log.info("Convite criado para {} (papel {}). Link de aceite: {}", invite.getEmail(), invite.getRole(), acceptLink);

        return InviteResponse.from(invite);
    }

    public List<InviteResponse> findPending() {
        return inviteRepository.findAllByTenantIdAndAcceptedAtIsNull(TenantContext.get()).stream()
                .map(InviteResponse::from)
                .toList();
    }

    @Transactional
    public AuthResponse accept(AcceptInviteRequest request) {
        Invite invite = inviteRepository.findByToken(request.token())
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));

        if (invite.isAccepted()) {
            throw new ConflictException("Este convite já foi utilizado.");
        }
        if (invite.isExpired()) {
            throw new ConflictException("Este convite expirou.");
        }

        planLimitService.assertCanCreateUser(invite.getTenantId(), userRepository.countByTenantId(invite.getTenantId()));

        User user = userRepository.save(
                User.builder()
                        .name(request.name())
                        .email(invite.getEmail())
                        .password(passwordEncoder.encode(request.password()))
                        .role(invite.getRole())
                        .tenantId(invite.getTenantId())
                        .build()
        );

        invite.setAcceptedAt(Instant.now());
        inviteRepository.save(invite);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getTenantId(), user.getRole().name());
    }
}
