package com.alertify.tracking.adapter.out.jpa.repository;

import com.alertify.tracking.adapter.out.jpa.entity.PriceHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistoryEntity, UUID> {

    List<PriceHistoryEntity> findByProductIdOrderByDetectedAtDesc(UUID productId, Pageable pageable);
}
