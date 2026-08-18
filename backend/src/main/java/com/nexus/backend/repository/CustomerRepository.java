package com.nexus.backend.repository;

import com.nexus.backend.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByTenantId(Long tenantId);

    Optional<Customer> findByIdAndTenantId(Long id, Long tenantId);
}
