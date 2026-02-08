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
    public void sendScrapeRequest(
            UUID productId,
            String url
    ) {

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