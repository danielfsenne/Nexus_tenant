package com.nexus.backend.repository;

import com.nexus.backend.domain.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {

    List<Invite> findAllByTenantIdAndAcceptedAtIsNull(Long tenantId);

    Optional<Invite> findByToken(String token);

    boolean existsByEmailAndTenantIdAndAcceptedAtIsNull(String email, Long tenantId);
}
