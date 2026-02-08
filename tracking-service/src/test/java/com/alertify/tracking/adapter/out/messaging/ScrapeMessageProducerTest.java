package com.alertify.tracking.adapter.out.messaging;

import com.alertify.common.event.ScrapeRequestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScrapeMessageProducerTest {

    @InjectMocks
    private ScrapeMessageProducer producer;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private final String EXCHANGE = "test.exchange";
    private final String ROUTING_KEY = "test.key";
    private final UUID productId = UUID.randomUUID();
    private final String url = "https://trendyol.com/urun";

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(producer, "exchange", EXCHANGE);
        ReflectionTestUtils.setField(producer, "routingKey", ROUTING_KEY);
    }

    @Test
    void should_Send_ScrapeRequest_Successfully() {

        producer.sendScrapeRequest(productId, url);

        ArgumentCaptor<ScrapeRequestEvent> eventCaptor = ArgumentCaptor.forClass(ScrapeRequestEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE),
                eq(ROUTING_KEY),
                eventCaptor.capture()
        );

        ScrapeRequestEvent sentEvent = eventCaptor.getValue();
        assertEquals(productId, sentEvent.getProductId());
        assertEquals(url, sentEvent.getUrl());
    }

    @Test
    void should_Wrap_AmqpException_In_RuntimeException() {

        doThrow(new AmqpException("Connection refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                producer.sendScrapeRequest(productId, url)
        );
        assertEquals("Message publishing failed", exception.getMessage());
    }

    @Test
    void should_Wrap_GenericException_In_RuntimeException() {

        doThrow(new NullPointerException("Oops"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                producer.sendScrapeRequest(productId, url)
        );
        assertEquals("Unexpected messaging error", exception.getMessage());
    }
}