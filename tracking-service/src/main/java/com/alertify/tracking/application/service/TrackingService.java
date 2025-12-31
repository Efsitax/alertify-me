package com.alertify.tracking.application.service;

import com.alertify.common.exception.AccessDeniedException;
import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.tracking.application.port.in.TrackingUseCase;
import com.alertify.tracking.application.port.out.TrackingPort;
import com.alertify.tracking.domain.model.TrackedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingService implements TrackingUseCase {

    private final TrackingPort port;

    @Override
    @Transactional
    public TrackedProduct createTrackedProduct(UUID userId, String url, BigDecimal targetPrice) {
        TrackedProduct domain = TrackedProduct.builder()
                .userId(userId)
                .url(url)
                .targetPrice(targetPrice)
                .build();
        return port.save(domain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrackedProduct> getTrackedProducts(UUID userId, Pageable pageable) {
        return port.findAllByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackedProduct> getProductsToScan(Instant threshold, Pageable pageable) {
        return port.findProductsToScan(threshold, pageable);
    }

    @Override
    @Transactional
    public TrackedProduct updateTrackedProduct(UUID userId, UUID productId, BigDecimal targetPrice, Boolean isActive) {
        TrackedProduct trackedProduct = port.findByProductId(productId);
        if (trackedProduct == null) {
            throw new ResourceNotFoundException("Tracked Product", "productId", productId.toString());
        } else if (!trackedProduct.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to update this tracked product.");
        } else {
            trackedProduct.setTargetPrice(targetPrice);
            trackedProduct.setIsActive(isActive);
            return port.save(trackedProduct);
        }
    }

    @Override
    @Transactional
    public void deleteTrackedProduct(UUID productId, UUID userId) {
        TrackedProduct trackedProduct = port.findByProductId(productId);
        if (trackedProduct == null) {
            throw new ResourceNotFoundException("Tracked Product", "productId", productId.toString());
        } else if (!trackedProduct.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to delete this tracked product.");
        } else {
            trackedProduct.setIsActive(false);
            port.save(trackedProduct);
        }
    }
}
