package com.nexus.backend.repository;

import com.nexus.backend.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByTenantId(Long tenantId, Pageable pageable);

    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);
}
