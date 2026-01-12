package com.alertify.tracking.adapter.out.jpa.mapper;

import com.alertify.tracking.adapter.out.jpa.entity.PriceHistoryEntity;
import com.alertify.tracking.adapter.out.jpa.entity.TrackedProductEntity;
import com.alertify.tracking.domain.model.PriceHistory;
import org.springframework.stereotype.Component;

@Component
public class PriceHistoryMapper {

    public PriceHistoryEntity toEntity(PriceHistory domain) {
        if (domain == null) return null;

        return PriceHistoryEntity.builder()
                .id(domain.getId())
                .product(TrackedProductEntity.builder().id(domain.getProductId()).build())
                .price(domain.getPrice())
                .detectedAt(domain.getDetectedAt())
                .build();
    }

    public PriceHistory toDomain(PriceHistoryEntity entity) {
        if (entity == null) return null;

        return PriceHistory.builder()
                .id(entity.getId())
                .price(entity.getPrice())
                .detectedAt(entity.getDetectedAt())
                .build();
    }
}
