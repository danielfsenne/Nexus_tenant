package com.nexus.backend.tenant;

import com.nexus.backend.domain.Plan;

public record TenantUsageResponse(
        String companyName,
        Plan plan,
        Usage usage,
        Limits limits
) {
    public record Usage(long users, long customers, long products) {
    }

    public record Limits(Integer maxUsers, Integer maxCustomers, Integer maxProducts) {
    }
}
