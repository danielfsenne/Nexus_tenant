package com.nexus.backend.product;

import com.nexus.backend.audit.AuditService;
import com.nexus.backend.common.PlanLimitService;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.Product;
import com.nexus.backend.repository.ProductRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final String ENTITY_TYPE = "PRODUCT";
    private static final String CACHE_NAME = "products";
    private static final String CACHE_KEY = "T(com.nexus.backend.security.TenantContext).get()";

    private final ProductRepository productRepository;
    private final PlanLimitService planLimitService;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository, PlanLimitService planLimitService, AuditService auditService) {
        this.productRepository = productRepository;
        this.planLimitService = planLimitService;
        this.auditService = auditService;
    }

    @Cacheable(value = CACHE_NAME, key = CACHE_KEY)
    public List<ProductResponse> findAll() {
        return productRepository.findAllByTenantId(TenantContext.get()).stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return ProductResponse.from(findOwnedByTenant(id));
    }

    @CacheEvict(value = CACHE_NAME, key = CACHE_KEY)
    public ProductResponse create(ProductRequest request) {
        Long tenantId = TenantContext.get();
        planLimitService.assertCanCreateProduct(tenantId, productRepository.countByTenantId(tenantId));

        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .tenantId(tenantId)
                .build();

        Product saved = productRepository.save(product);
        auditService.record(AuditAction.CREATED, ENTITY_TYPE, saved.getId(), saved.getName());
        return ProductResponse.from(saved);
    }

    @CacheEvict(value = CACHE_NAME, key = CACHE_KEY)
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOwnedByTenant(id);
        product.setName(request.name());
        product.setPrice(request.price());
        Product saved = productRepository.save(product);
        auditService.record(AuditAction.UPDATED, ENTITY_TYPE, saved.getId(), saved.getName());
        return ProductResponse.from(saved);
    }

    @CacheEvict(value = CACHE_NAME, key = CACHE_KEY)
    public void delete(Long id) {
        Product product = findOwnedByTenant(id);
        productRepository.delete(product);
        auditService.record(AuditAction.DELETED, ENTITY_TYPE, product.getId(), product.getName());
    }

    private Product findOwnedByTenant(Long id) {
        return productRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }
}
