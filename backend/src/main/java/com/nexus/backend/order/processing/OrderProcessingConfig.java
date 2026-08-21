package com.nexus.backend.order.processing;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Fila principal de processamento de vendas + fila de retry (com atraso via TTL
 * por mensagem) + dead-letter queue final, após esgotar as tentativas.
 */
@Configuration
public class OrderProcessingConfig {

    public static final String EXCHANGE = "nexus.orders";
    public static final String QUEUE_MAIN = "order.processing";
    public static final String QUEUE_RETRY = "order.processing.retry";
    public static final String QUEUE_DLQ = "order.processing.dlq";
    public static final String ROUTING_KEY_CREATED = "order.created";
    public static final String ROUTING_KEY_RETRY = "order.retry";
    public static final String ROUTING_KEY_FAILED = "order.failed";

    @Bean
    public DirectExchange orderProcessingExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue orderProcessingQueue() {
        return QueueBuilder.durable(QUEUE_MAIN).build();
    }

    @Bean
    public Queue orderProcessingRetryQueue() {
        return QueueBuilder.durable(QUEUE_RETRY)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_CREATED)
                .build();
    }

    @Bean
    public Queue orderProcessingDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding bindMainQueue(Queue orderProcessingQueue, DirectExchange orderProcessingExchange) {
        return BindingBuilder.bind(orderProcessingQueue).to(orderProcessingExchange).with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding bindRetryQueue(Queue orderProcessingRetryQueue, DirectExchange orderProcessingExchange) {
        return BindingBuilder.bind(orderProcessingRetryQueue).to(orderProcessingExchange).with(ROUTING_KEY_RETRY);
    }

    @Bean
    public Binding bindDlq(Queue orderProcessingDlq, DirectExchange orderProcessingExchange) {
        return BindingBuilder.bind(orderProcessingDlq).to(orderProcessingExchange).with(ROUTING_KEY_FAILED);
    }

    @Bean
    public MessageConverter messageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
