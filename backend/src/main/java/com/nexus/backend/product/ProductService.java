package com.nexus.backend.product;

import com.nexus.backend.common.PlanLimitService;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.Product;
import com.nexus.backend.repository.ProductRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final PlanLimitService planLimitService;

    public ProductService(ProductRepository productRepository, PlanLimitService planLimitService) {
        this.productRepository = productRepository;
        this.planLimitService = planLimitService;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAllByTenantId(TenantContext.get()).stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return ProductResponse.from(findOwnedByTenant(id));
    }

    public ProductResponse create(ProductRequest request) {
        Long tenantId = TenantContext.get();
        planLimitService.assertCanCreateProduct(tenantId, productRepository.countByTenantId(tenantId));

        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .tenantId(tenantId)
                .build();

        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOwnedByTenant(id);
        product.setName(request.name());
        product.setPrice(request.price());
        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        productRepository.delete(findOwnedByTenant(id));
    }

    private Product findOwnedByTenant(Long id) {
        return productRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }
}
