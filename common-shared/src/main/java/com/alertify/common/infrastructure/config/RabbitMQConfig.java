/*
 * Copyright 2026 efsitax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alertify.common.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Slf4j
@Configuration
public class RabbitMQConfig {

    @Value("${alertify.rabbitmq.exchange:scrape.exchange}")
    private String exchangeName;

    @Value("${alertify.rabbitmq.queue:scrape.queue}")
    private String requestQueueName;
    @Value("${alertify.rabbitmq.routing-key:scrape.key}")
    private String requestRoutingKey;

    @Value("${alertify.rabbitmq.completed-queue:scrape.completed.queue}")
    private String completedQueueName;
    @Value("${alertify.rabbitmq.completed-routing-key:scrape.completed.key}")
    private String completedRoutingKey;

    private static final String DLQ_EXCHANGE_NAME = "scrape.dlx";
    private static final String DLQ_QUEUE_NAME = "scrape.dlq";
    private static final String DLQ_ROUTING_KEY = "scrape.dlq.key";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLQ_EXCHANGE_NAME);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE_NAME).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue requestQueue() {
        return QueueBuilder.durable(requestQueueName)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding requestBinding() {
        return BindingBuilder.bind(requestQueue()).to(exchange()).with(requestRoutingKey);
    }

    @Bean
    public Queue completedQueue() {
        return QueueBuilder.durable(completedQueueName)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding completedBinding() {
        return BindingBuilder.bind(completedQueue()).to(exchange()).with(completedRoutingKey);
    }

    @Bean
    @Primary
    public MessageConverter converter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of(
                "com.alertify.*",
                "java.util.*",
                "java.time.*",
                "java.math.*",
                "java.lang.*"));
        return converter;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public ApplicationRunner initRabbitMQ(RabbitAdmin admin) {
        return args -> {
            try {
                admin.declareExchange(deadLetterExchange());
                admin.declareQueue(deadLetterQueue());
                admin.declareBinding(deadLetterBinding());

                admin.declareExchange(exchange());
                admin.declareQueue(requestQueue());
                admin.declareBinding(requestBinding());

                admin.declareQueue(completedQueue());
                admin.declareBinding(completedBinding());

                log.info("RabbitMQ: Request, Completed and DLQ lines are configured successfully!");
            } catch (Exception e) {
                log.error("RabbitMQ Init Error: Failed to configure queues.", e);
            }
        };
    }
}