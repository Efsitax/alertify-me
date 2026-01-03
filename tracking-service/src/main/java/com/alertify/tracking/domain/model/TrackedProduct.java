package com.alertify.tracking.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackedProduct {

    private UUID id;
    private UUID userId;
    private String url;
    private String productName;
    private BigDecimal currentPrice;
    private Boolean inStock;
    private String currency;
    private BigDecimal targetPrice;
    private Boolean isActive;
    private Instant lastCheckedAt;
    private Instant createdAt;
}
