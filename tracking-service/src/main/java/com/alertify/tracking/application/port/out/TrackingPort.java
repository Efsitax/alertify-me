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

package com.alertify.tracking.application.port.out;

import com.alertify.tracking.domain.model.PriceHistory;
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

    List<PriceHistory> findPriceHistoryByProductId(UUID productId, Pageable pageable);
}
