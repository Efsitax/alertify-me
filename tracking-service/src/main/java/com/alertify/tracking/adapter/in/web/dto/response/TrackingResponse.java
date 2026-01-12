package com.alertify.tracking.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TrackingResponse(
        UUID id,
        UUID userId,
        String url,
        String productName,
        BigDecimal currentPrice,
        Boolean inStock,
        String currency,
        BigDecimal targetPrice,
        Boolean isActive,
        Instant lastCheckedAt,
        Instant createdAt
) {
}
