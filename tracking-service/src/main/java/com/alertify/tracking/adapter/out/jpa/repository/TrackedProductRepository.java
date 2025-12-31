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
import java.util.UUID;

@Repository
public interface TrackedProductRepository extends JpaRepository<TrackedProductEntity, UUID> {

    Page<TrackedProductEntity> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT p FROM TrackedProductEntity p WHERE p.isActive = true AND (p.lastCheckedAt IS NULL OR p.lastCheckedAt < :threshold)")
    List<TrackedProductEntity> findProductsToScan(@Param("threshold") Instant threshold, Pageable pageable);
}
