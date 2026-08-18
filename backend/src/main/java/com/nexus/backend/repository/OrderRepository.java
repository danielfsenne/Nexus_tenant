package com.nexus.backend.repository;

import com.nexus.backend.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByTenantId(Long tenantId);

    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);
}
