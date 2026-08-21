package com.nexus.backend.order.processing;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long tenantId,
        Long customerId,
        String customerName,
        BigDecimal total
) {
}
