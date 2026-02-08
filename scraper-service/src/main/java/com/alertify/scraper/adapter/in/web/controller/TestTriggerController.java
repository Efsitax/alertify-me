package com.alertify.scraper.adapter.in.web.controller;

import com.alertify.common.event.ScrapeRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestTriggerController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${alertify.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${alertify.rabbitmq.routing-key}")
    private String routingKey;

    @GetMapping("/send")
    public String sendTestMessage(@RequestParam String url) {

        ScrapeRequestEvent event = new ScrapeRequestEvent(UUID.randomUUID(), url);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
        return "Test message sent for URL: " + url + "\nWatch the console...";
    }
}
