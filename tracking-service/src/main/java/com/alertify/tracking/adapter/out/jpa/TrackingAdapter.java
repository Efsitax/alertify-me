package com.alertify.tracking.adapter.out.jpa;

import com.alertify.tracking.adapter.out.jpa.mapper.PriceHistoryMapper;
import com.alertify.tracking.adapter.out.jpa.mapper.TrackedProductMapper;
import com.alertify.tracking.adapter.out.jpa.repository.PriceHistoryRepository;
import com.alertify.tracking.adapter.out.jpa.repository.TrackedProductRepository;
import com.alertify.tracking.application.port.out.TrackingPort;
import com.alertify.tracking.domain.model.PriceHistory;
import com.alertify.tracking.domain.model.TrackedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TrackingAdapter implements TrackingPort {

    private final TrackedProductRepository trackedProductRepository;
    private final TrackedProductMapper trackedProductMapper;
    
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceHistoryMapper priceHistoryMapper;

    @Override
    public TrackedProduct save(TrackedProduct trackedProduct) {

        var entity = trackedProductMapper.toEntity(trackedProduct);
        var savedEntity = trackedProductRepository.save(entity);
        return trackedProductMapper.toDomain(savedEntity);
    }

    @Override
    public Page<TrackedProduct> findAllByUserId(UUID userId, Pageable pageable) {
        return trackedProductRepository.findAllByUserId(userId, pageable)
                .map(trackedProductMapper::toDomain);
    }

    @Override
    public List<TrackedProduct> findProductsToScan(Instant threshold, Pageable pageable) {
        return trackedProductRepository.findProductsToScan(threshold, pageable).stream()
                .map(trackedProductMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<TrackedProduct> findByProductId(UUID id) {
        return trackedProductRepository.findById(id).map(trackedProductMapper::toDomain);
    }

    @Override
    public List<PriceHistory> findPriceHistoryByProductId(UUID productId, Pageable pageable) {
        return  priceHistoryRepository.findByProductIdOrderByDetectedAtDesc(productId, pageable).stream()
                .map(priceHistoryMapper::toDomain)
                .toList();
    }

}
