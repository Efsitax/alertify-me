package com.alertify.scraper.adapter.in.messaging;

import com.alertify.common.event.PriceScrapeCompletedEvent;
import com.alertify.common.event.ScrapeRequestEvent;
import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.common.exception.ScrapeFailedException;
import com.alertify.scraper.application.port.out.ScraperPort;
import com.alertify.scraper.domain.model.ScrapedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScrapeRequestConsumerTest {

    @InjectMocks
    private ScrapeRequestConsumer consumer;

    @Mock
    private ScraperPort port;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private final String EXCHANGE = "scrape.exchange";
    private final String ROUTING_KEY = "scrape.completed.key";

    private ScrapeRequestEvent validEvent;
    private UUID testUuid;

    @BeforeEach
    void  setUp() {
        ReflectionTestUtils.setField(consumer, "exchange", EXCHANGE);
        ReflectionTestUtils.setField(consumer, "completedRoutingKey", ROUTING_KEY);

        testUuid = UUID.randomUUID();
        validEvent = new ScrapeRequestEvent(testUuid, "https://amazon.com/example-product");
    }

    @Test
    void should_Process_Successfully_And_Send_Event() {
        ScrapedProduct scrapedProduct = ScrapedProduct.builder()
                .productName("iPhone 15")
                .price(new BigDecimal("50000"))
                .currency("TRY")
                .inStock(true)
                .build();

        when(port.fetchProduct(validEvent.getUrl())).thenReturn(scrapedProduct);
        consumer.consumeMessage(validEvent);

        ArgumentCaptor<PriceScrapeCompletedEvent> captor = ArgumentCaptor.forClass(PriceScrapeCompletedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE),
                eq(ROUTING_KEY),
                captor.capture()
        );

        PriceScrapeCompletedEvent event = captor.getValue();
        assertEquals(testUuid, event.getProductId());
        assertEquals("iPhone 15", event.getProductName());
        assertEquals(new BigDecimal("50000"), event.getPrice());
        assertTrue(event.getInStock());
        assertNotNull(event.getCheckedAt(), "CheckedAt timestamp should not be null");
    }

    @Test
    void should_Not_Retry_When_ResourceNotFoundException_Occurs() {
        doThrow(new ResourceNotFoundException("Product", "id", "404"))
                .when(port).fetchProduct(validEvent.getUrl());
        assertDoesNotThrow(() -> consumer.consumeMessage(validEvent));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void should_Throw_Exception_To_Trigger_Retry_When_ScrapeFailed() {
        doThrow(new ScrapeFailedException("Timeout"))
                .when(port).fetchProduct(validEvent.getUrl());
        assertThrows(
                ScrapeFailedException.class,
                () -> consumer.consumeMessage(validEvent)
        );
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void should_Throw_RuntimeException_On_Unexpected_Error() {
        doThrow(new RuntimeException("Unexpected Bug"))
                .when(port).fetchProduct(validEvent.getUrl());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> consumer.consumeMessage(validEvent));
        assertEquals("Unexpected scraper error", exception.getMessage());
    }
}
