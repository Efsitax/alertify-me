package com.alertify.tracking.adapter.out.messaging;

import com.alertify.common.event.ScrapeRequestEvent;
import com.alertify.tracking.application.port.out.ScrapePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScrapeMessageProducer implements ScrapePort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${alertify.rabbitmq.exchange}")
    private String exchange;

    @Value("${alertify.rabbitmq.routing-key}")
    private String routingKey;

    @Override
    public void sendScrapeRequest(UUID productId, String url) {

        ScrapeRequestEvent event = new ScrapeRequestEvent(productId, url);

        log.info("Preparing to send scrape request. Product ID: {} | URL: {}", productId, url);

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);

            log.info("Scrape request published successfully to RabbitMQ. Product ID: {}", productId);

        } catch (AmqpException e) {
            log.error("Failed to publish message to RabbitMQ! Product ID: {} - Error: {}", productId, e.getMessage());
            throw new RuntimeException("Message publishing failed", e);

        } catch (Exception e) {
            log.error("Unexpected error while publishing message. Product ID: {} - Error: {}", productId, e.getMessage());
            throw new RuntimeException("Unexpected messaging error", e);
        }
    }
}