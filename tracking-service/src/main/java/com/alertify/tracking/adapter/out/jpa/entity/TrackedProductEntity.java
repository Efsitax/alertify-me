package com.alertify.tracking.adapter.out.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tracked_products")
public class TrackedProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 2048)
    private String url;
    private String productName;
    private BigDecimal currentPrice;
    private Boolean inStock;
    @Column(length = 3)
    private String currency;
    private BigDecimal targetPrice;
    private Boolean isActive;

    @Column(updatable = false)
    private Instant createdAt;
    private Instant lastCheckedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<PriceHistoryEntity> priceHistory= new ArrayList<>();

    @PrePersist
    protected void onCreate() {

        createdAt = Instant.now();
        if (isActive == null) {
            isActive = true;
        }
    }
}
