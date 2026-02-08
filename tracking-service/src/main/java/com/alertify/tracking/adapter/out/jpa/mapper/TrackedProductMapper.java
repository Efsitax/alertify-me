/*
 * Copyright 2026 efsitax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alertify.tracking.adapter.out.jpa.mapper;

import com.alertify.tracking.adapter.out.jpa.entity.PriceHistoryEntity;
import com.alertify.tracking.adapter.out.jpa.entity.TrackedProductEntity;
import com.alertify.tracking.domain.model.TrackedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrackedProductMapper {

    private final PriceHistoryMapper priceHistoryMapper;

    public TrackedProductEntity toEntity(
            TrackedProduct domain
    ) {

        if (domain == null) return null;

        TrackedProductEntity entity = TrackedProductEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .url(domain.getUrl())
                .productName(domain.getProductName())
                .currentPrice(domain.getCurrentPrice())
                .inStock(domain.getInStock())
                .currency(domain.getCurrency())
                .targetPrice(domain.getTargetPrice())
                .isActive(domain.getIsActive())
                .lastCheckedAt(domain.getLastCheckedAt())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getPriceHistory() != null && !domain.getPriceHistory().isEmpty()) {
            List<PriceHistoryEntity> historyEntities = domain.getPriceHistory().stream()
                    .map(priceHistoryMapper::toEntity)
                    .toList();
            historyEntities.forEach(h -> h.setProduct(entity));
            entity.setPriceHistory(new ArrayList<>(historyEntities));
        }

        return entity;
    }

    public TrackedProduct toDomain(
            TrackedProductEntity entity
    ) {

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
                .priceHistory(entity.getPriceHistory() != null
                        ? new ArrayList<>(entity.getPriceHistory().stream().map(priceHistoryMapper::toDomain).toList())
                        : new ArrayList<>())
                .build();
    }
}