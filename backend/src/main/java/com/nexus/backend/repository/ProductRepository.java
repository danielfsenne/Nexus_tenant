package com.nexus.backend.repository;

import com.nexus.backend.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<Product> findAllByTenantIdAndNameContainingIgnoreCase(Long tenantId, String name, Pageable pageable);

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantId(Long tenantId);
}
