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
import com.alertify.tracking.domain.model.PriceHistory;
import org.springframework.stereotype.Component;

@Component
public class PriceHistoryMapper {

    public PriceHistoryEntity toEntity(
            PriceHistory domain
    ) {

        if (domain == null) return null;

        return PriceHistoryEntity.builder()
                .id(domain.getId())
                .product(TrackedProductEntity.builder().id(domain.getProductId()).build())
                .price(domain.getPrice())
                .detectedAt(domain.getDetectedAt())
                .build();
    }

    public PriceHistory toDomain(
            PriceHistoryEntity entity
    ) {

        if (entity == null) return null;

        return PriceHistory.builder()
                .id(entity.getId())
                .price(entity.getPrice())
                .detectedAt(entity.getDetectedAt())
                .build();
    }
}
