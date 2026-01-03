package com.alertify.tracking.application.port.out;

import com.alertify.tracking.domain.model.TrackedProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackingPort {

    TrackedProduct save(TrackedProduct trackedProduct);
    Page<TrackedProduct> findAllByUserId(UUID userId, Pageable pageable);
    List<TrackedProduct> findProductsToScan(Instant threshold, Pageable pageable);
    Optional<TrackedProduct> findByProductId(UUID id);
}
