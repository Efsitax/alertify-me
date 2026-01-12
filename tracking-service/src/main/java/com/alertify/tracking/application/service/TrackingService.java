package com.alertify.tracking.application.service;

import com.alertify.common.exception.AccessDeniedException;
import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.tracking.application.port.in.ScrapeResultsUseCase;
import com.alertify.tracking.application.port.in.TrackingUseCase;
import com.alertify.tracking.application.port.out.ScrapePort;
import com.alertify.tracking.application.port.out.TrackingPort;
import com.alertify.tracking.domain.model.PriceHistory;
import com.alertify.tracking.domain.model.TrackedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService implements TrackingUseCase, ScrapeResultsUseCase {

    private final TrackingPort productPort;
    private final ScrapePort scrapePort;

    @Override
    @Transactional
    public TrackedProduct createTrackedProduct(UUID userId, String url, BigDecimal targetPrice) {
        TrackedProduct domain = TrackedProduct.builder()
                .userId(userId)
                .url(url)
                .targetPrice(targetPrice)
                .isActive(true)
                .build();

        TrackedProduct savedProduct = productPort.save(domain);
        log.info("New tracking created for User: {} with Product ID: {}", userId, savedProduct.getId());
        scrapePort.sendScrapeRequest(savedProduct.getId(), savedProduct.getUrl());

        return savedProduct;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrackedProduct> getTrackedProducts(UUID userId, Pageable pageable) {
        return productPort.findAllByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackedProduct> getProductsToScan(Instant threshold, Pageable pageable) {
        return productPort.findProductsToScan(threshold, pageable);
    }

    @Override
    @Transactional
    public TrackedProduct updateTrackedProduct(UUID userId, UUID productId, BigDecimal targetPrice, Boolean isActive) {
        Optional<TrackedProduct> trackedProductOpt = productPort.findByProductId(productId);

        if (trackedProductOpt.isEmpty()) {
            throw new ResourceNotFoundException("Tracked Product", "id", productId.toString());
        }

        TrackedProduct trackedProduct = trackedProductOpt.get();

        if (!trackedProduct.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to update this tracked product.");
        }

        trackedProduct.setTargetPrice(targetPrice);
        if (isActive != null) {
            trackedProduct.setIsActive(isActive);
        }

        log.info("Product updated: {}", productId);
        return productPort.save(trackedProduct);
    }

    @Override
    @Transactional
    public void deleteTrackedProduct(UUID productId, UUID userId) {
        Optional<TrackedProduct> trackedProductOpt = productPort.findByProductId(productId);

        if (trackedProductOpt.isEmpty()) {
            throw new ResourceNotFoundException("Tracked Product", "id", productId.toString());
        }

        TrackedProduct trackedProduct = trackedProductOpt.get();

        if (!trackedProduct.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to delete this tracked product.");
        }

        trackedProduct.setIsActive(false);
        productPort.save(trackedProduct);
        log.info("Product soft-deleted: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceHistory> getPriceHistory(UUID productId, UUID userId, Pageable pageable) {
        Optional<TrackedProduct> trackedProductOpt = productPort.findByProductId(productId);

        if (trackedProductOpt.isEmpty()) {
            throw new ResourceNotFoundException("Tracked Product", "id", productId.toString());
        }

        TrackedProduct trackedProduct = trackedProductOpt.get();

        if (!trackedProduct.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view the price history of this product.");
        }

        return productPort.findPriceHistoryByProductId(productId, pageable);
    }

    @Override
    @Transactional
    public boolean handleScrapeResult(
            UUID productId,
            String productName,
            BigDecimal price,
            Boolean inStock,
            String currency,
            Instant checkedAt
    ) {
        log.info("Processing scrape result for Product ID: {}", productId);

        Optional<TrackedProduct> trackedProductOpt = productPort.findByProductId(productId);

        if (trackedProductOpt.isEmpty()) {
            log.error("Scraper returned an unknown Product ID: {}. Skipping update.", productId);
            return false;
        }

        TrackedProduct trackedProduct = trackedProductOpt.get();

        trackedProduct.setProductName(productName);
        trackedProduct.setInStock(inStock);
        trackedProduct.setCurrency(currency);

        trackedProduct.updatePrice(price, checkedAt);

        productPort.save(trackedProduct);
        log.info("Price updated successfully for Product ID: {}. New Price: {} {}", productId, price, currency);
        return true;
    }
}