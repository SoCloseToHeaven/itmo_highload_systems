package ru.ifmo.highload.discount.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String DISCOUNT_ORDER_QUEUE = "discount.order.created";
    private static final String ROUTING_KEY_ORDER_CREATED = "order.created";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue discountOrderQueue() {
        return new Queue(DISCOUNT_ORDER_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding(Queue discountOrderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(discountOrderQueue).to(orderExchange).with(ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    @Primary
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
