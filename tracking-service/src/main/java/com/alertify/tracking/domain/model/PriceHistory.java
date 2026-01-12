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
public class PriceHistory {

    private UUID id;
    private UUID productId;
    private BigDecimal price;
    private Instant detectedAt;
}
