package com.nexus.backend.order;

import com.nexus.backend.audit.AuditService;
import com.nexus.backend.common.CsvUtil;
import com.nexus.backend.common.PageResponse;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.Customer;
import com.nexus.backend.domain.Order;
import com.nexus.backend.order.processing.OrderCreatedEvent;
import com.nexus.backend.order.processing.OrderProcessingProducer;
import com.nexus.backend.repository.CustomerRepository;
import com.nexus.backend.repository.OrderRepository;
import com.nexus.backend.security.TenantContext;
import com.nexus.backend.websocket.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final String ENTITY_TYPE = "ORDER";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final OrderProcessingProducer orderProcessingProducer;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            AuditService auditService,
            NotificationService notificationService,
            OrderProcessingProducer orderProcessingProducer
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.orderProcessingProducer = orderProcessingProducer;
    }

    public PageResponse<OrderResponse> findAll(int page, int size, Long customerId) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        var tenantId = TenantContext.get();
        var result = (customerId == null)
                ? orderRepository.findAllByTenantId(tenantId, pageable)
                : orderRepository.findAllByTenantIdAndCustomerId(tenantId, customerId, pageable);
        return PageResponse.from(result, OrderResponse::from);
    }

    public byte[] exportCsv(Long customerId) {
        var pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id").descending());
        var tenantId = TenantContext.get();
        var result = (customerId == null)
                ? orderRepository.findAllByTenantId(tenantId, pageable)
                : orderRepository.findAllByTenantIdAndCustomerId(tenantId, customerId, pageable);

        Map<Long, String> customerNameById = customerRepository.findAllByTenantId(tenantId, pageable).getContent().stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));

        StringBuilder csv = new StringBuilder("﻿");
        csv.append(CsvUtil.row("Cliente", "Total", "Criado em"));
        for (Order order : result.getContent()) {
            String customerName = customerNameById.getOrDefault(order.getCustomerId(), "#" + order.getCustomerId());
            csv.append(CsvUtil.row(customerName, order.getTotal(), order.getCreatedAt()));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public OrderResponse findById(Long id) {
        return OrderResponse.from(findOwnedByTenant(id));
    }

    public OrderResponse create(OrderRequest request) {
        Long tenantId = TenantContext.get();

        Customer customer = customerRepository.findByIdAndTenantId(request.customerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        Order order = Order.builder()
                .customerId(request.customerId())
                .total(request.total())
                .tenantId(tenantId)
                .build();

        Order saved = orderRepository.save(order);
        auditService.record(AuditAction.CREATED, ENTITY_TYPE, saved.getId(), "total " + saved.getTotal());
        notificationService.notifyTenant(tenantId, "ORDER_CREATED", "Nova venda registrada: R$ " + saved.getTotal());

        orderProcessingProducer.publish(new OrderCreatedEvent(
                saved.getId(), tenantId, customer.getId(), customer.getName(), saved.getTotal()
        ));

        return OrderResponse.from(saved);
    }

    private Order findOwnedByTenant(Long id) {
        return orderRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));
    }
}
