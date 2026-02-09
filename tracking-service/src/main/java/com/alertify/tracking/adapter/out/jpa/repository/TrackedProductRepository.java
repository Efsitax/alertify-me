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

package com.alertify.tracking.adapter.out.jpa.repository;

import com.alertify.tracking.adapter.out.jpa.entity.TrackedProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackedProductRepository extends JpaRepository<TrackedProductEntity, UUID> {

    Page<TrackedProductEntity> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT p FROM TrackedProductEntity p WHERE p.isActive = true AND (p.lastCheckedAt IS NULL OR p.lastCheckedAt < :threshold)")
    List<TrackedProductEntity> findProductsToScan(@Param("threshold") Instant threshold, Pageable pageable);

    Optional<TrackedProductEntity> findByUserIdAndUrl(UUID userId, String url);
}
