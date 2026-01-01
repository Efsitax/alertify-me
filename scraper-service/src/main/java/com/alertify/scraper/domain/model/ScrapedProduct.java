package com.alertify.scraper.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ScrapedProduct {

    private String productName;
    private BigDecimal price;
    private String currency;
    private String imageUrl;
    private Boolean inStock;
    private String shopName;
}
