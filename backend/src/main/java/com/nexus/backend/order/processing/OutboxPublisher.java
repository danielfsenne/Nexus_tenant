package com.nexus.backend.order.processing;

import com.nexus.backend.domain.OutboxEvent;
import com.nexus.backend.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

/**
 * Publica na fila os eventos gravados na mesma transação da mudança de
 * negócio (padrão Outbox), evitando o problema de "dual write": salvar no
 * banco e publicar na fila não são atômicos, então publicamos de forma
 * assíncrona a partir de uma tabela que faz parte da própria transação.
 */
@Component
@ConditionalOnProperty(name = "nexus.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, RabbitTemplate rabbitTemplate, JsonMapper jsonMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByPublishedAtIsNullOrderByIdAsc();
        for (OutboxEvent event : pending) {
            try {
                OrderCreatedEvent payload = jsonMapper.readValue(event.getPayload(), OrderCreatedEvent.class);
                rabbitTemplate.convertAndSend(event.getExchange(), event.getRoutingKey(), payload);
                event.setPublishedAt(Instant.now());
            } catch (Exception ex) {
                log.error("Falha ao publicar evento outbox {}", event.getId(), ex);
            }
        }
    }
}
