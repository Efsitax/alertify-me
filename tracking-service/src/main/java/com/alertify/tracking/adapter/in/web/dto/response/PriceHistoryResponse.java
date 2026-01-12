package com.alertify.tracking.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceHistoryResponse(
        BigDecimal price,
        Instant detectedAt
) {
}
