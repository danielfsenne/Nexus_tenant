package com.nexus.backend.customer;

import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.Customer;
import com.nexus.backend.repository.CustomerRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAllByTenantId(TenantContext.get()).stream()
                .map(CustomerResponse::from)
                .toList();
    }

    public CustomerResponse findById(Long id) {
        return CustomerResponse.from(findOwnedByTenant(id));
    }

    public CustomerResponse create(CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .tenantId(TenantContext.get())
                .build();

        return CustomerResponse.from(customerRepository.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOwnedByTenant(id);
        customer.setName(request.name());
        customer.setEmail(request.email());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public void delete(Long id) {
        Customer customer = findOwnedByTenant(id);
        customerRepository.delete(customer);
    }

    private Customer findOwnedByTenant(Long id) {
        return customerRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }
}
