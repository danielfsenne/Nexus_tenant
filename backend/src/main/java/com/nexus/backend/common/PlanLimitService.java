package com.nexus.backend.common;

import com.nexus.backend.domain.PlanLimits;
import com.nexus.backend.domain.Tenant;
import com.nexus.backend.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class PlanLimitService {

    private final TenantRepository tenantRepository;

    public PlanLimitService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public void assertCanCreateUser(Long tenantId, long currentCount) {
        assertWithinLimit(tenantId, currentCount, PlanLimits::maxUsers, "usuários");
    }

    public void assertCanCreateCustomer(Long tenantId, long currentCount) {
        assertWithinLimit(tenantId, currentCount, PlanLimits::maxCustomers, "clientes");
    }

    public void assertCanCreateProduct(Long tenantId, long currentCount) {
        assertWithinLimit(tenantId, currentCount, PlanLimits::maxProducts, "produtos");
    }

    private void assertWithinLimit(
            Long tenantId,
            long currentCount,
            Function<PlanLimits, Integer> limitExtractor,
            String resourceLabel
    ) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        Integer max = limitExtractor.apply(PlanLimits.of(tenant.getPlan()));

        if (max != null && currentCount >= max) {
            throw new PlanLimitExceededException(
                    "Limite de %s do plano %s atingido (%d).".formatted(resourceLabel, tenant.getPlan(), max)
            );
        }
    }
}
