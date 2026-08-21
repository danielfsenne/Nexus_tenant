package com.nexus.backend.maintenance;

import com.nexus.backend.repository.InviteRepository;
import com.nexus.backend.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Limpeza de dados operacionais expirados. É uma manutenção global (não
 * escopada por tenant) — convites e tokens de reset são efêmeros por natureza
 * e não carregam dado de negócio sensível entre empresas.
 */
@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);

    private final InviteRepository inviteRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public MaintenanceService(InviteRepository inviteRepository, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.inviteRepository = inviteRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public MaintenanceResult cleanupExpired() {
        Instant now = Instant.now();

        long invitesDeleted = inviteRepository.deleteByAcceptedAtIsNullAndExpiresAtBefore(now);
        long expiredTokens = passwordResetTokenRepository.deleteByExpiresAtBefore(now);
        long usedTokens = passwordResetTokenRepository.deleteByUsedAtIsNotNull();
        long tokensDeleted = expiredTokens + usedTokens;

        log.info("Limpeza de manutenção: {} convites expirados e {} tokens de redefinição removidos", invitesDeleted, tokensDeleted);

        return new MaintenanceResult(invitesDeleted, tokensDeleted);
    }
}
