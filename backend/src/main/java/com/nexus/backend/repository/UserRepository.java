package com.nexus.backend.repository;

import com.nexus.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * O e-mail é único por tenant, não globalmente — a mesma pessoa pode ser
     * admin em várias empresas. Pega a conta mais antiga com esse e-mail.
     */
    Optional<User> findFirstByEmailOrderByIdAsc(String email);

    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    long countByTenantId(Long tenantId);

    List<User> findAllByTenantId(Long tenantId);
}
