package com.nexus.backend.product;

import com.nexus.backend.audit.AuditService;
import com.nexus.backend.common.CsvUtil;
import com.nexus.backend.common.PageResponse;
import com.nexus.backend.common.PlanLimitService;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.Product;
import com.nexus.backend.repository.ProductRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ProductService {

    private static final String ENTITY_TYPE = "PRODUCT";

    private final ProductRepository productRepository;
    private final PlanLimitService planLimitService;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository, PlanLimitService planLimitService, AuditService auditService) {
        this.productRepository = productRepository;
        this.planLimitService = planLimitService;
        this.auditService = auditService;
    }

    public PageResponse<ProductResponse> findAll(int page, int size, String search) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        var tenantId = TenantContext.get();
        var result = (search == null || search.isBlank())
                ? productRepository.findAllByTenantId(tenantId, pageable)
                : productRepository.findAllByTenantIdAndNameContainingIgnoreCase(tenantId, search.trim(), pageable);
        return PageResponse.from(result, ProductResponse::from);
    }

    public byte[] exportCsv(String search) {
        var pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id").descending());
        var tenantId = TenantContext.get();
        var result = (search == null || search.isBlank())
                ? productRepository.findAllByTenantId(tenantId, pageable)
                : productRepository.findAllByTenantIdAndNameContainingIgnoreCase(tenantId, search.trim(), pageable);

        StringBuilder csv = new StringBuilder("﻿");
        csv.append(CsvUtil.row("Nome", "Preço", "Criado em"));
        for (Product product : result.getContent()) {
            csv.append(CsvUtil.row(product.getName(), product.getPrice(), product.getCreatedAt()));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
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

        Product saved = productRepository.save(product);
        auditService.record(AuditAction.CREATED, ENTITY_TYPE, saved.getId(), saved.getName());
        return ProductResponse.from(saved);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOwnedByTenant(id);
        product.setName(request.name());
        product.setPrice(request.price());
        Product saved = productRepository.save(product);
        auditService.record(AuditAction.UPDATED, ENTITY_TYPE, saved.getId(), saved.getName());
        return ProductResponse.from(saved);
    }

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
