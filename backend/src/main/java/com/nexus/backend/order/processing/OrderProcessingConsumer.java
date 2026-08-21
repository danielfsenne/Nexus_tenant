package com.nexus.backend.order.processing;

import com.nexus.backend.audit.AuditService;
import com.nexus.backend.domain.AuditAction;
import com.nexus.backend.websocket.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Processa vendas de forma assíncrona (simulado). Gatilhos de demonstração no
 * nome do cliente:
 * - contém "RETRY": falha nas 2 primeiras tentativas e se recupera na 3ª;
 * - contém "FALHA": falha sempre, até esgotar as tentativas e cair na DLQ.
 * Qualquer outro nome é processado com sucesso de primeira.
 */
@Component
public class OrderProcessingConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessingConsumer.class);
    private static final String ENTITY_TYPE = "ORDER";
    private static final String SYSTEM_ACTOR = "worker";

    private final AuditService auditService;
    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;
    private final int maxAttempts;
    private final long baseDelayMs;

    public OrderProcessingConsumer(
            AuditService auditService,
            NotificationService notificationService,
            RabbitTemplate rabbitTemplate,
            @Value("${nexus.order-processing.max-attempts}") int maxAttempts,
            @Value("${nexus.order-processing.base-delay-ms}") long baseDelayMs
    ) {
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.rabbitTemplate = rabbitTemplate;
        this.maxAttempts = maxAttempts;
        this.baseDelayMs = baseDelayMs;
    }

    @RabbitListener(queues = OrderProcessingConfig.QUEUE_MAIN)
    public void handle(OrderCreatedEvent event, @Header(name = "x-attempt", required = false) Integer attemptHeader) {
        int attempt = attemptHeader == null ? 0 : attemptHeader;

        try {
            simulateProcessing(event, attempt);
            onSuccess(event, attempt);
        } catch (OrderProcessingException ex) {
            onFailure(event, attempt, ex);
        }
    }

    private void simulateProcessing(OrderCreatedEvent event, int attempt) {
        String name = event.customerName() == null ? "" : event.customerName().toUpperCase();

        if (name.contains("FALHA")) {
            throw new OrderProcessingException("Falha simulada (gatilho FALHA) na tentativa " + (attempt + 1));
        }
        if (name.contains("RETRY") && attempt < 2) {
            throw new OrderProcessingException("Falha simulada (gatilho RETRY) na tentativa " + (attempt + 1));
        }
    }

    private void onSuccess(OrderCreatedEvent event, int attempt) {
        log.info("Venda {} processada com sucesso na tentativa {}", event.orderId(), attempt + 1);

        auditService.recordForTenant(
                event.tenantId(), null, SYSTEM_ACTOR,
                AuditAction.PROCESSED, ENTITY_TYPE, event.orderId(),
                "processada na tentativa " + (attempt + 1)
        );

        notificationService.notifyTenant(
                event.tenantId(), "ORDER_PROCESSED",
                "Venda #" + event.orderId() + " processada com sucesso"
        );
    }

    private void onFailure(OrderCreatedEvent event, int attempt, Exception ex) {
        int nextAttempt = attempt + 1;
        log.warn("Falha ao processar venda {} (tentativa {}/{}): {}", event.orderId(), nextAttempt, maxAttempts, ex.getMessage());

        if (nextAttempt < maxAttempts) {
            long delayMs = baseDelayMs * (1L << attempt);
            rabbitTemplate.convertAndSend(
                    OrderProcessingConfig.EXCHANGE, OrderProcessingConfig.ROUTING_KEY_RETRY, event,
                    message -> {
                        message.getMessageProperties().setExpiration(String.valueOf(delayMs));
                        message.getMessageProperties().setHeader("x-attempt", nextAttempt);
                        return message;
                    }
            );
            log.info("Venda {} reagendada para nova tentativa em {}ms", event.orderId(), delayMs);
            return;
        }

        log.error("Venda {} esgotou {} tentativas, movendo para a dead-letter queue", event.orderId(), maxAttempts);

        rabbitTemplate.convertAndSend(
                OrderProcessingConfig.EXCHANGE, OrderProcessingConfig.ROUTING_KEY_FAILED, event,
                message -> {
                    message.getMessageProperties().setHeader("x-attempt", nextAttempt);
                    return message;
                }
        );

        auditService.recordForTenant(
                event.tenantId(), null, SYSTEM_ACTOR,
                AuditAction.PROCESSING_FAILED, ENTITY_TYPE, event.orderId(),
                "esgotadas " + maxAttempts + " tentativas"
        );

        notificationService.notifyTenant(
                event.tenantId(), "ORDER_PROCESSING_FAILED",
                "Falha ao processar a venda #" + event.orderId() + " após " + maxAttempts + " tentativas"
        );
    }
}
