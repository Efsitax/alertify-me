package com.alertify.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.alertify.tracking", "com.alertify.common"})
public class TrackingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrackingServiceApplication.class, args);
    }
}