package com.nexus.backend.customer;

import com.nexus.backend.audit.AuditService;
import com.nexus.backend.common.PlanLimitService;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.Customer;
import com.nexus.backend.repository.CustomerRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private static final String ENTITY_TYPE = "CUSTOMER";
    private static final String CACHE_NAME = "customers";
    private static final String CACHE_KEY = "T(com.nexus.backend.security.TenantContext).get()";

    private final CustomerRepository customerRepository;
    private final PlanLimitService planLimitService;
    private final AuditService auditService;

    public CustomerService(CustomerRepository customerRepository, PlanLimitService planLimitService, AuditService auditService) {
        this.customerRepository = customerRepository;
        this.planLimitService = planLimitService;
        this.auditService = auditService;
    }

    @Cacheable(value = CACHE_NAME, key = CACHE_KEY)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAllByTenantId(TenantContext.get()).stream()
                .map(CustomerResponse::from)
                .toList();
    }

    public CustomerResponse findById(Long id) {
        return CustomerResponse.from(findOwnedByTenant(id));
    }

    @CacheEvict(value = CACHE_NAME, key = CACHE_KEY)
    public CustomerResponse create(CustomerRequest request) {
        Long tenantId = TenantContext.get();
        planLimitService.assertCanCreateCustomer(tenantId, customerRepository.countByTenantId(tenantId));

        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .tenantId(tenantId)
                .build();

        Customer saved = customerRepository.save(customer);
        auditService.record(AuditAction.CREATED, ENTITY_TYPE, saved.getId(), saved.getName());
        return CustomerResponse.from(saved);
    }

    @CacheEvict(value = CACHE_NAME, key = CACHE_KEY)
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOwnedByTenant(id);
        customer.setName(request.name());
        customer.setEmail(request.email());
        Customer saved = customerRepository.save(customer);
        auditService.record(AuditAction.UPDATED, ENTITY_TYPE, saved.getId(), saved.getName());
        return CustomerResponse.from(saved);
    }

    @CacheEvict(value = CACHE_NAME, key = CACHE_KEY)
    public void delete(Long id) {
        Customer customer = findOwnedByTenant(id);
        customerRepository.delete(customer);
        auditService.record(AuditAction.DELETED, ENTITY_TYPE, customer.getId(), customer.getName());
    }

    private Customer findOwnedByTenant(Long id) {
        return customerRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }
}
