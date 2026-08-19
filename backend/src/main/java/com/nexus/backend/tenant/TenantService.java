package com.nexus.backend.tenant;

import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.PlanLimits;
import com.nexus.backend.domain.Tenant;
import com.nexus.backend.repository.CustomerRepository;
import com.nexus.backend.repository.ProductRepository;
import com.nexus.backend.repository.TenantRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public TenantService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    public TenantUsageResponse currentUsage() {
        Long tenantId = TenantContext.get();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        PlanLimits limits = PlanLimits.of(tenant.getPlan());

        return new TenantUsageResponse(
                tenant.getName(),
                tenant.getPlan(),
                new TenantUsageResponse.Usage(
                        userRepository.countByTenantId(tenantId),
                        customerRepository.countByTenantId(tenantId),
                        productRepository.countByTenantId(tenantId)
                ),
                new TenantUsageResponse.Limits(limits.maxUsers(), limits.maxCustomers(), limits.maxProducts())
        );
    }
}
