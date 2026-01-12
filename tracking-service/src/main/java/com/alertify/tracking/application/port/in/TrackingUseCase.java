package com.alertify.tracking.application.port.in;

import com.alertify.tracking.domain.model.PriceHistory;
import com.alertify.tracking.domain.model.TrackedProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TrackingUseCase {

    TrackedProduct createTrackedProduct(UUID userId, String url, BigDecimal targetPrice);
    Page<TrackedProduct> getTrackedProducts(UUID userId, Pageable pageable);
    List<TrackedProduct> getProductsToScan(Instant threshold, Pageable pageable);
    TrackedProduct updateTrackedProduct(UUID userId, UUID productId, BigDecimal targetPrice, Boolean isActive);
    void deleteTrackedProduct(UUID productId, UUID userId);

    List<PriceHistory> getPriceHistory(UUID productId, UUID userId, Pageable pageable);
}
