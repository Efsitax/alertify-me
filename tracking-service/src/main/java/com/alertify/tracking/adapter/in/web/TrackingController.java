package com.alertify.tracking.adapter.in.web;

import com.alertify.tracking.adapter.in.web.dto.request.CreateTrackingRequest;
import com.alertify.tracking.adapter.in.web.dto.TrackingResponse;
import com.alertify.tracking.adapter.in.web.dto.request.UpdateTrackingRequest;
import com.alertify.tracking.application.port.in.TrackingUseCase;
import com.alertify.tracking.domain.model.TrackedProduct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trackings")
public class TrackingController {

    @Value("${alertify.tracking.scan-interval-minutes:30}")
    private int scanIntervalMinutes;
    private final TrackingUseCase useCase;

    @PostMapping
    public ResponseEntity<TrackingResponse> createTracking(
            @RequestParam UUID userId,
            @Valid @RequestBody CreateTrackingRequest request
    ) {
        TrackedProduct createdProduct = useCase.createTrackedProduct(userId, request.url(), request.targetPrice());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(createdProduct));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TrackingResponse>> getUserTrackings(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(
                useCase.getTrackedProducts(userId, pageable)
                        .map(this::toResponse)
        );
    }

    @GetMapping("/scan-candidates")
    public ResponseEntity<List<TrackingResponse>> getProductsToScan(
            @RequestParam(defaultValue = "10") int limit
    ) {
        Instant threshold = Instant.now().minus(scanIntervalMinutes, java.time.temporal.ChronoUnit.MINUTES);
        Pageable pageable = PageRequest.of(0, limit);
        return ResponseEntity.ok(
                useCase.getProductsToScan(threshold, pageable).stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<TrackingResponse> updateTracking(
            @PathVariable UUID productId,
            @RequestParam UUID userId,
            @Valid @RequestBody UpdateTrackingRequest request
    ) {
        TrackedProduct updated = useCase.updateTrackedProduct(userId, productId, request.targetPrice(), request.isActive());
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteTracking(
            @PathVariable UUID productId,
            @RequestParam UUID userId) {
        useCase.deleteTrackedProduct(productId, userId);
        return ResponseEntity.noContent().build();
    }

    private TrackingResponse toResponse(TrackedProduct domain) {
        return new TrackingResponse(
                domain.getId(),
                domain.getUserId(),
                domain.getUrl(),
                domain.getTargetPrice(),
                domain.getCurrentPrice(),
                domain.getIsActive(),
                domain.getLastCheckedAt(),
                domain.getCreatedAt()
        );
    }
}
