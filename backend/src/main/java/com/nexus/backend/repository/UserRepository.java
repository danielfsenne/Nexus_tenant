package com.nexus.backend.repository;

import com.nexus.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    long countByTenantId(Long tenantId);

    List<User> findAllByTenantId(Long tenantId);
}
