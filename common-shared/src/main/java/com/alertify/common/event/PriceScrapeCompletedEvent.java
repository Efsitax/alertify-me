package com.alertify.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceScrapeCompletedEvent implements Serializable {
    private UUID productId;
    private String url;
    private String productName;
    private Boolean inStock;
    private BigDecimal price;
    private String currency;
    private Instant checkedAt;
}