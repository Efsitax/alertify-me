package com.alertify.tracking.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface ScrapeResultsUseCase {

    boolean handleScrapeResult(
            UUID productId,
            String productName,
            BigDecimal price,
            Boolean inStock,
            String currency,
            Instant checkedAt);
}