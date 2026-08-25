package com.nexus.backend.repository;

import com.nexus.backend.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    long deleteByExpiresAtBefore(Instant instant);

    long deleteByUsedAtIsNotNull();
}
