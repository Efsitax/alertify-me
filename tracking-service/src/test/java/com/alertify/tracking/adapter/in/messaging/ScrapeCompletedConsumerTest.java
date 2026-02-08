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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScrapeCompletedConsumerTest {

    @InjectMocks
    private ScrapeCompletedConsumer consumer;

    @Mock
    private ScrapeResultsUseCase useCase;

    private PriceScrapeCompletedEvent validEvent;
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validEvent = new PriceScrapeCompletedEvent(
                productId,
                "https://amazon.com/product",
                "PlayStation 5",
                true,
                new BigDecimal("20000"),
                "TRY",
                Instant.now()
        );
    }

    @Test
    void should_Process_Successfully_When_UseCase_Returns_True() {

        when(useCase.handleScrapeResult(
                eq(productId),
                anyString(),
                any(BigDecimal.class),
                anyBoolean(),
                anyString(),
                any(Instant.class)
        )).thenReturn(true);

        assertDoesNotThrow(() -> consumer.consumeScrapeResult(validEvent));

        verify(useCase, times(1)).handleScrapeResult(
                eq(productId),
                eq("PlayStation 5"),
                eq(new BigDecimal("20000")),
                eq(true),
                eq("TRY"),
                eq(validEvent.getCheckedAt())
        );
    }

    @Test
    void should_Log_Warning_But_Succeed_When_UseCase_Returns_False() {

        when(useCase.handleScrapeResult(any(), any(), any(), anyBoolean(), any(), any()))
                .thenReturn(false);
        assertDoesNotThrow(() -> consumer.consumeScrapeResult(validEvent));
        verify(useCase).handleScrapeResult(any(), any(), any(), anyBoolean(), any(), any());
    }

    @Test
    void should_Throw_Exception_When_UseCase_Fails() {

        doThrow(new RuntimeException("Database connection failed"))
                .when(useCase).handleScrapeResult(any(), any(), any(), anyBoolean(), any(), any());
        assertThrows(RuntimeException.class, () -> consumer.consumeScrapeResult(validEvent));
    }
}