package com.alertify.tracking.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TrackingResponse(
        UUID id,
        UUID userId,
        String url,
        BigDecimal targetPrice,
        BigDecimal currentPrice,
        Boolean isActive,
        Instant lastCheckedAt,
        Instant createdAt
) {
}
