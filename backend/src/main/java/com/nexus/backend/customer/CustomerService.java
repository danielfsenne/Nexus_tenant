package com.nexus.backend.customer;

import com.nexus.backend.audit.AuditService;
import com.nexus.backend.common.PageResponse;
import com.nexus.backend.common.PlanLimitService;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.Customer;
import com.nexus.backend.repository.CustomerRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private static final String ENTITY_TYPE = "CUSTOMER";

    private final CustomerRepository customerRepository;
    private final PlanLimitService planLimitService;
    private final AuditService auditService;

    public CustomerService(CustomerRepository customerRepository, PlanLimitService planLimitService, AuditService auditService) {
        this.customerRepository = customerRepository;
        this.planLimitService = planLimitService;
        this.auditService = auditService;
    }

    public PageResponse<CustomerResponse> findAll(int page, int size, String search) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        var tenantId = TenantContext.get();
        var result = (search == null || search.isBlank())
                ? customerRepository.findAllByTenantId(tenantId, pageable)
                : customerRepository.search(tenantId, search.trim(), pageable);
        return PageResponse.from(result, CustomerResponse::from);
    }

    public CustomerResponse findById(Long id) {
        return CustomerResponse.from(findOwnedByTenant(id));
    }

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

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOwnedByTenant(id);
        customer.setName(request.name());
        customer.setEmail(request.email());
        Customer saved = customerRepository.save(customer);
        auditService.record(AuditAction.UPDATED, ENTITY_TYPE, saved.getId(), saved.getName());
        return CustomerResponse.from(saved);
    }

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
