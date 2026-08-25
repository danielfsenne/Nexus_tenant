package com.nexus.backend.order;

import com.nexus.backend.audit.AuditService;
import com.nexus.backend.common.CsvUtil;
import com.nexus.backend.common.IdempotencyService;
import com.nexus.backend.common.PageResponse;
import com.nexus.backend.common.ResourceNotFoundException;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.domain.Customer;
import com.nexus.backend.domain.Order;
import com.nexus.backend.domain.OutboxEvent;
import com.nexus.backend.order.processing.OrderCreatedEvent;
import com.nexus.backend.order.processing.OrderProcessingConfig;
import com.nexus.backend.repository.CustomerRepository;
import com.nexus.backend.repository.OrderRepository;
import com.nexus.backend.repository.OutboxEventRepository;
import com.nexus.backend.security.TenantContext;
import com.nexus.backend.websocket.NotificationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final String ENTITY_TYPE = "ORDER";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final IdempotencyService idempotencyService;
    private final JsonMapper jsonMapper;
    private final OrderService self;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            OutboxEventRepository outboxEventRepository,
            AuditService auditService,
            NotificationService notificationService,
            IdempotencyService idempotencyService,
            JsonMapper jsonMapper,
            @Lazy OrderService self
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.idempotencyService = idempotencyService;
        this.jsonMapper = jsonMapper;
        this.self = self;
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

    public OrderResponse create(OrderRequest request, String idempotencyKey) {
        Long tenantId = TenantContext.get();

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return self.doCreate(request, tenantId);
        }

        return idempotencyService.execute(tenantId, idempotencyKey, OrderResponse.class,
                () -> self.doCreate(request, tenantId));
    }

    @Transactional
    public OrderResponse doCreate(OrderRequest request, Long tenantId) {
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

        OrderCreatedEvent event = new OrderCreatedEvent(
                saved.getId(), tenantId, customer.getId(), customer.getName(), saved.getTotal()
        );
        outboxEventRepository.save(OutboxEvent.builder()
                .tenantId(tenantId)
                .eventType("ORDER_CREATED")
                .exchange(OrderProcessingConfig.EXCHANGE)
                .routingKey(OrderProcessingConfig.ROUTING_KEY_CREATED)
                .payload(jsonMapper.writeValueAsString(event))
                .build());

        return OrderResponse.from(saved);
    }

    private Order findOwnedByTenant(Long id) {
        return orderRepository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));
    }
}
