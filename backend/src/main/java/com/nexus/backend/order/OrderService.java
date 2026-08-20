package com.nexus.backend.order;

import com.nexus.backend.audit.AuditService;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.Order;
import com.nexus.backend.repository.CustomerRepository;
import com.nexus.backend.repository.OrderRepository;
import com.nexus.backend.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private static final String ENTITY_TYPE = "ORDER";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository, AuditService auditService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAllByTenantId(TenantContext.get()).stream()
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse findById(Long id) {
        return OrderResponse.from(findOwnedByTenant(id));
    }

    public OrderResponse create(OrderRequest request) {
        Long tenantId = TenantContext.get();

        customerRepository.findByIdAndTenantId(request.customerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        Order order = Order.builder()
                .customerId(request.customerId())
                .total(request.total())
                .tenantId(tenantId)
                .build();

        Order saved = orderRepository.save(order);
        auditService.record(AuditAction.CREATED, ENTITY_TYPE, saved.getId(), "total " + saved.getTotal());
        return OrderResponse.from(saved);
    }

    private Order findOwnedByTenant(Long id) {
        return orderRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));
    }
}
