package com.treatz.dispatchservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "treatz_exchange";
    public static final String QUEUE_NAME = "dispatch_queue";
    // This is the specific event we are listening for!
    public static final String ORDER_READY_ROUTING_KEY = "order.status.ready_for_pickup";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        // Tell the sorting room to send all "ready_for_pickup" mail to our mailbox.
        return BindingBuilder.bind(queue).to(exchange).with(ORDER_READY_ROUTING_KEY);
    }

    // We need the JSON converter to understand the messages from the Order Service
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // The new event we are now listening for
    public static final String ORDER_DELIVERED_ROUTING_KEY = "order.status.delivered";

    // A second binding for the same queue
    @Bean
    public Binding deliveredBinding(Queue queue, TopicExchange exchange) {
        // Tell the exchange to ALSO send "delivered" mail to our dispatch_queue.
        return BindingBuilder.bind(queue).to(exchange).with(ORDER_DELIVERED_ROUTING_KEY);
    }
}