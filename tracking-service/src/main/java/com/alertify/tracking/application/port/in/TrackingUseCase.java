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
