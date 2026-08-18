package com.nexus.backend.order;

import com.nexus.backend.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(Long id, Long customerId, BigDecimal total, Instant createdAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getTotal(), order.getCreatedAt());
    }
}
