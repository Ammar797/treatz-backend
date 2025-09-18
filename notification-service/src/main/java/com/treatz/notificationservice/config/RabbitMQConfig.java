package com.treatz.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // These names must EXACTLY match the ones in the Order Service
    public static final String EXCHANGE_NAME = "treatz_exchange";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    // This will be the name of our service's private mailbox
    public static final String QUEUE_NAME = "notification_queue";

    @Bean
    public TopicExchange exchange() {
        // This declares the central sorting hub (it won't create a new one if it already exists)
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queue() {
        // This creates our durable mailbox
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        // This is the forwarding address. It tells the exchange:
        // "Send any message with the 'order.placed' label to my 'notification_queue' mailbox."
        return BindingBuilder.bind(queue).to(exchange).with(ORDER_PLACED_ROUTING_KEY);
    }
}