package com.alertify.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.alertify.scraper", "com.alertify.common"})
public class ScraperServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScraperServiceApplication.class, args);
    }
}