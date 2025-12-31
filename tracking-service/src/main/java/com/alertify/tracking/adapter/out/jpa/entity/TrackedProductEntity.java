package com.alertify.tracking.adapter.out.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tracked_products")
public class TrackedProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 2048)
    private String url;
    private BigDecimal targetPrice;
    private BigDecimal currentPrice;
    private Boolean isActive;

    @Column(updatable = false)
    private Instant createdAt;
    private Instant lastCheckedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (isActive == null) {
            isActive = true;
        }
    }
}
