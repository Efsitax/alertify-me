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

package com.alertify.tracking.adapter.in.scheduling;

import com.alertify.tracking.application.port.in.TrackingUseCase;
import com.alertify.tracking.application.port.out.ScrapePort;
import com.alertify.tracking.domain.model.TrackedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingSchedulerTest {

    @InjectMocks
    private TrackingScheduler scheduler;

    @Mock
    private TrackingUseCase trackingUseCase;

    @Mock
    private ScrapePort scrapePort;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(scheduler, "scanIntervalMinutes", 30);
        ReflectionTestUtils.setField(scheduler, "batchSize", 10);
    }

    @Test
    void should_DoNothing_When_NoProductsToScan() {

        when(trackingUseCase.getProductsToScan(any(Instant.class), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        scheduler.scheduleScraping();

        verify(scrapePort, never()).sendScrapeRequest(any(), any());
    }

    @Test
    void should_SendScrapeRequests_When_ProductsFound() {

        TrackedProduct product1 = TrackedProduct.builder()
                .id(UUID.randomUUID())
                .url("https://site.com/p1")
                .build();

        TrackedProduct product2 = TrackedProduct.builder()
                .id(UUID.randomUUID())
                .url("https://site.com/p2")
                .build();

        when(trackingUseCase.getProductsToScan(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(product1, product2));

        scheduler.scheduleScraping();

        verify(scrapePort, times(1)).sendScrapeRequest(product1.getId(), product1.getUrl());
        verify(scrapePort, times(1)).sendScrapeRequest(product2.getId(), product2.getUrl());
    }

    @Test
    void should_ContinueProcessing_When_OneRequestFails() {

        TrackedProduct product1 = TrackedProduct.builder()
                .id(UUID.randomUUID())
                .url("https://site.com/p1")
                .build();

        TrackedProduct product2 = TrackedProduct.builder()
                .id(UUID.randomUUID())
                .url("https://site.com/p2")
                .build();

        when(trackingUseCase.getProductsToScan(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(product1, product2));

        doThrow(new RuntimeException("Queue Error"))
                .when(scrapePort).sendScrapeRequest(product1.getId(), product1.getUrl());

        scheduler.scheduleScraping();

        verify(scrapePort).sendScrapeRequest(product1.getId(), product1.getUrl());
        verify(scrapePort).sendScrapeRequest(product2.getId(), product2.getUrl());
    }
}