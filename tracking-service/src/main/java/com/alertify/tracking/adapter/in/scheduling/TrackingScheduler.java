package com.alertify.tracking.adapter.in.scheduling;

import com.alertify.tracking.application.port.in.TrackingUseCase;
import com.alertify.tracking.application.port.out.ScrapePort;
import com.alertify.tracking.domain.model.TrackedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingScheduler {

    private final TrackingUseCase trackingUseCase;
    private final ScrapePort scrapePort;

    @Value("${alertify.tracking.scan-interval-minutes}")
    private int scanIntervalMinutes;

    @Value("${alertify.tracking.scan-batch-size}")
    private int batchSize;

    @Scheduled(fixedRateString = "${alertify.tracking.scan-scheduler-rate-ms}", initialDelay = 10000)
    public void scheduleScraping() {
        log.info("Cron Job triggered: Checking for products to scrape...");

        Instant threshold = Instant.now().minus(scanIntervalMinutes, ChronoUnit.MINUTES);

        Pageable pageable = PageRequest.of(0, batchSize);
        List<TrackedProduct> productsToScan = trackingUseCase.getProductsToScan(threshold, pageable);

        if (productsToScan.isEmpty()) {
            log.info("No products found needing update.");
            return;
        }

        log.info("Found {} products to scan. Sending to Scraper Service...", productsToScan.size());

        for (TrackedProduct product : productsToScan) {
            try {
                scrapePort.sendScrapeRequest(product.getId(), product.getUrl());
                log.debug("Sent scrape request for Product ID: {}", product.getId());
            } catch (Exception e) {
                log.error("Failed to send scrape request for Product ID: {}", product.getId(), e);
            }
        }
    }
}
