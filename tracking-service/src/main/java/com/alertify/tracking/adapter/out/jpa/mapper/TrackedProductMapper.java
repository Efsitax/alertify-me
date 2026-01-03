package com.alertify.tracking.adapter.out.jpa.mapper;

import com.alertify.tracking.adapter.out.jpa.entity.TrackedProductEntity;
import com.alertify.tracking.domain.model.TrackedProduct;
import org.springframework.stereotype.Component;

@Component
public class TrackedProductMapper {

    public TrackedProductEntity toEntity(TrackedProduct domain) {
        if (domain == null) return null;

        return new TrackedProductEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getUrl(),
                domain.getProductName(),
                domain.getCurrentPrice(),
                domain.getInStock(),
                domain.getCurrency(),
                domain.getTargetPrice(),
                domain.getIsActive(),
                domain.getLastCheckedAt(),
                domain.getCreatedAt()
        );
    }

    public TrackedProduct toDomain(TrackedProductEntity entity) {
        if (entity == null) return null;

        return TrackedProduct.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .url(entity.getUrl())
                .productName(entity.getProductName())
                .currentPrice(entity.getCurrentPrice())
                .inStock(entity.getInStock())
                .currency(entity.getCurrency())
                .targetPrice(entity.getTargetPrice())
                .isActive(entity.getIsActive())
                .lastCheckedAt(entity.getLastCheckedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
