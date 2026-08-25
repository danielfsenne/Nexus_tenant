package com.nexus.backend.common;

import com.nexus.backend.domain.PlanLimits;
import com.nexus.backend.domain.Tenant;
import com.nexus.backend.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Checa o limite do plano travando a linha do tenant (PESSIMISTIC_WRITE)
 * antes de contar os registros atuais. Sem a trava, duas requisições
 * concorrentes de criação poderiam ler a mesma contagem, ambas passar na
 * checagem e ambas inserir — estourando o limite (TOCTOU clássico). Quem
 * chama precisa estar dentro de uma transação para a trava valer até o
 * INSERT que segue a checagem.
 */
@Service
public class PlanLimitService {

    private final TenantRepository tenantRepository;

    public PlanLimitService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public void assertCanCreateUser(Long tenantId, Supplier<Long> currentCountSupplier) {
        assertWithinLimit(tenantId, currentCountSupplier, PlanLimits::maxUsers, "usuários");
    }

    public void assertCanCreateCustomer(Long tenantId, Supplier<Long> currentCountSupplier) {
        assertWithinLimit(tenantId, currentCountSupplier, PlanLimits::maxCustomers, "clientes");
    }

    public void assertCanCreateProduct(Long tenantId, Supplier<Long> currentCountSupplier) {
        assertWithinLimit(tenantId, currentCountSupplier, PlanLimits::maxProducts, "produtos");
    }

    private void assertWithinLimit(
            Long tenantId,
            Supplier<Long> currentCountSupplier,
            Function<PlanLimits, Integer> limitExtractor,
            String resourceLabel
    ) {
        Tenant tenant = tenantRepository.lockById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        long currentCount = currentCountSupplier.get();
        Integer max = limitExtractor.apply(PlanLimits.of(tenant.getPlan()));

        if (max != null && currentCount >= max) {
            throw new PlanLimitExceededException(
                    "Limite de %s do plano %s atingido (%d).".formatted(resourceLabel, tenant.getPlan(), max)
            );
        }
    }
}
