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

package com.alertify.tracking.adapter.in.messaging;

import com.alertify.common.event.PriceScrapeCompletedEvent;
import com.alertify.tracking.application.port.in.ScrapeResultsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScrapeCompletedConsumer {

    private final ScrapeResultsUseCase useCase;

    @RabbitListener(queues = "${alertify.rabbitmq.completed-queue}")
    public void consumeScrapeResult(PriceScrapeCompletedEvent event) {

        log.info("Scrape result received. ID: {} | Product: {} | Price: {}",
                event.getProductId(), event.getProductName(), event.getPrice());

        try {
            boolean isUpdated = useCase.handleScrapeResult(
                    event.getProductId(),
                    event.getProductName(),
                    event.getPrice(),
                    event.getInStock(),
                    event.getCurrency(),
                    event.getCheckedAt()
            );

            if (isUpdated) {
                log.info("Database updated successfully for Product ID: {}", event.getProductId());
            } else {
                log.warn("Update SKIPPED for Product ID: {} (Product not found)", event.getProductId());
            }
        } catch (Exception e) {
            log.error("Failed to update product price in DB. ID: {} - Error: {}",
                    event.getProductId(), e.getMessage());
            throw e;
        }
    }
}