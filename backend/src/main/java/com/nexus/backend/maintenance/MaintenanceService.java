package com.nexus.backend.maintenance;

import com.nexus.backend.repository.EmailVerificationTokenRepository;
import com.nexus.backend.repository.InviteRepository;
import com.nexus.backend.repository.PasswordResetTokenRepository;
import com.nexus.backend.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Limpeza de dados operacionais expirados. É uma manutenção global (não
 * escopada por tenant) — convites e tokens são efêmeros por natureza e não
 * carregam dado de negócio sensível entre empresas.
 */
@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);

    private final InviteRepository inviteRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public MaintenanceService(
            InviteRepository inviteRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.inviteRepository = inviteRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public MaintenanceResult cleanupExpired() {
        Instant now = Instant.now();

        long invitesDeleted = inviteRepository.deleteByAcceptedAtIsNullAndExpiresAtBefore(now);

        long expiredResetTokens = passwordResetTokenRepository.deleteByExpiresAtBefore(now);
        long usedResetTokens = passwordResetTokenRepository.deleteByUsedAtIsNotNull();
        long resetTokensDeleted = expiredResetTokens + usedResetTokens;

        long expiredVerificationTokens = emailVerificationTokenRepository.deleteByExpiresAtBefore(now);
        long usedVerificationTokens = emailVerificationTokenRepository.deleteByUsedAtIsNotNull();
        long verificationTokensDeleted = expiredVerificationTokens + usedVerificationTokens;

        long expiredRefreshTokens = refreshTokenRepository.deleteByExpiresAtBefore(now);
        long revokedRefreshTokens = refreshTokenRepository.deleteByRevokedAtIsNotNull();
        long refreshTokensDeleted = expiredRefreshTokens + revokedRefreshTokens;

        long tokensDeleted = resetTokensDeleted + verificationTokensDeleted + refreshTokensDeleted;

        log.info("Limpeza de manutenção: {} convites expirados e {} tokens removidos", invitesDeleted, tokensDeleted);

        return new MaintenanceResult(invitesDeleted, tokensDeleted);
    }
}
