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

package com.alertify.scraper.adapter.in.messaging;

import com.alertify.common.event.PriceScrapeCompletedEvent;
import com.alertify.common.event.ScrapeRequestEvent;
import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.scraper.application.port.out.ScraperPort;
import com.alertify.common.exception.ScrapeFailedException;
import com.alertify.scraper.domain.model.ScrapedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScrapeRequestConsumer {

    private final ScraperPort scraperPort;
    private final RabbitTemplate rabbitTemplate;

    @Value("${alertify.rabbitmq.exchange}")
    private String exchange;

    @Value("${alertify.rabbitmq.completed-routing-key}")
    private String completedRoutingKey;

    @RabbitListener(queues = "${alertify.rabbitmq.queue}")
    public void consumeMessage(
            ScrapeRequestEvent event
    ) {

        log.info("Scrape Request received for Product ID: {} | URL: {}", event.getProductId(), event.getUrl());
        try {
            ScrapedProduct product = scraperPort.fetchProduct(event.getUrl());

            PriceScrapeCompletedEvent completedEvent = new PriceScrapeCompletedEvent(
                    event.getProductId(),
                    event.getUrl(),
                    product.getProductName(),
                    product.getInStock(),
                    product.getPrice(),
                    product.getCurrency(),
                    Instant.now()
            );

            rabbitTemplate.convertAndSend(exchange, completedRoutingKey, completedEvent);
            log.info("Scrape Successful. Result sent for ID: {}", event.getProductId());

        } catch (ResourceNotFoundException e) {
            log.warn("Permanent Error (Will NOT retry): {}", e.getMessage());

        } catch (ScrapeFailedException e) {
            log.error("Transient Error (Will RETRY): {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Unexpected Critical Error (Sending to DLQ): {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected scraper error", e);
        }
    }
}