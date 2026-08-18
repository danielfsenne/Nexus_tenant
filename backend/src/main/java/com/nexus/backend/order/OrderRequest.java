package com.nexus.backend.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderRequest(
        @NotNull Long customerId,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal total
) {
}
