package com.ecommerce.common.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Exchange Names
    public static final String DOMAIN_EVENTS_EXCHANGE = "domain.events.exchange";
    public static final String SAGA_COMMANDS_EXCHANGE = "saga.commands.exchange";

    // Queue Names
    public static final String ORDER_EVENTS_QUEUE = "order.events.queue";
    public static final String PRODUCT_EVENTS_QUEUE = "product.events.queue";
    public static final String PAYMENT_EVENTS_QUEUE = "payment.events.queue";
    public static final String SAGA_COMMANDS_QUEUE = "saga.commands.queue";

    // Routing Keys
    public static final String ORDER_EVENTS_ROUTING_KEY = "order.events";
    public static final String PRODUCT_EVENTS_ROUTING_KEY = "product.events";
    public static final String PAYMENT_EVENTS_ROUTING_KEY = "payment.events";
    public static final String SAGA_COMMANDS_ROUTING_KEY = "saga.commands";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // Domain Events Exchange
    @Bean
    public TopicExchange domainEventsExchange() {
        return new TopicExchange(DOMAIN_EVENTS_EXCHANGE);
    }

    // Saga Commands Exchange
    @Bean
    public DirectExchange sagaCommandsExchange() {
        return new DirectExchange(SAGA_COMMANDS_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue orderEventsQueue() {
        return QueueBuilder.durable(ORDER_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue productEventsQueue() {
        return QueueBuilder.durable(PRODUCT_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue paymentEventsQueue() {
        return QueueBuilder.durable(PAYMENT_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue sagaCommandsQueue() {
        return QueueBuilder.durable(SAGA_COMMANDS_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding orderEventsBinding() {
        return BindingBuilder
            .bind(orderEventsQueue())
            .to(domainEventsExchange())
            .with(ORDER_EVENTS_ROUTING_KEY);
    }

    @Bean
    public Binding productEventsBinding() {
        return BindingBuilder
            .bind(productEventsQueue())
            .to(domainEventsExchange())
            .with(PRODUCT_EVENTS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentEventsBinding() {
        return BindingBuilder
            .bind(paymentEventsQueue())
            .to(domainEventsExchange())
            .with(PAYMENT_EVENTS_ROUTING_KEY);
    }

    @Bean
    public Binding sagaCommandsBinding() {
        return BindingBuilder
            .bind(sagaCommandsQueue())
            .to(sagaCommandsExchange())
            .with(SAGA_COMMANDS_ROUTING_KEY);
    }
}