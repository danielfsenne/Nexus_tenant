package com.nexus.backend.customer;

import com.nexus.backend.domain.Customer;

import java.time.Instant;

public record CustomerResponse(Long id, String name, String email, Instant createdAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getCreatedAt());
    }
}
