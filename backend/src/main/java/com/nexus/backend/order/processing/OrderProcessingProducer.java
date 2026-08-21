package com.nexus.backend.order.processing;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingProducer {

    private final RabbitTemplate rabbitTemplate;

    public OrderProcessingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(OrderProcessingConfig.EXCHANGE, OrderProcessingConfig.ROUTING_KEY_CREATED, event);
    }
}
