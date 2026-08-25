package com.nexus.backend.repository;

import com.nexus.backend.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findAllByTenantId(Long tenantId, Pageable pageable);

    @Query("""
            select c from Customer c
            where c.tenantId = :tenantId
              and (lower(c.name) like lower(concat('%', :search, '%'))
                or lower(c.email) like lower(concat('%', :search, '%')))
            """)
    Page<Customer> search(@Param("tenantId") Long tenantId, @Param("search") String search, Pageable pageable);

    Optional<Customer> findByIdAndTenantId(Long id, Long tenantId);

    long countByTenantId(Long tenantId);
}
