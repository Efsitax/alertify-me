package com.alertify.tracking.adapter.out.jpa;

import com.alertify.tracking.adapter.out.jpa.mapper.TrackedProductMapper;
import com.alertify.tracking.adapter.out.jpa.repository.TrackedProductRepository;
import com.alertify.tracking.application.port.out.TrackingPort;
import com.alertify.tracking.domain.model.TrackedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TrackingAdapter implements TrackingPort {

    private final TrackedProductRepository repository;
    private final TrackedProductMapper mapper;

    @Override
    public TrackedProduct save(TrackedProduct trackedProduct) {
        var entity = mapper.toEntity(trackedProduct);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Page<TrackedProduct> findAllByUserId(UUID userId, Pageable pageable) {
        return repository.findAllByUserId(userId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<TrackedProduct> findProductsToScan(Instant threshold, Pageable pageable) {
        return repository.findProductsToScan(threshold, pageable).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public TrackedProduct findByProductId(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow();
    }

}
