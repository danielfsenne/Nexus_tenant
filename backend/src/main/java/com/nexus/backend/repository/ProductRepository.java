package com.nexus.backend.repository;

import com.nexus.backend.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByTenantId(Long tenantId);

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantId(Long tenantId);
}
