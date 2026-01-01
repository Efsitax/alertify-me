package com.alertify.scraper.application.port.in;

import com.alertify.scraper.domain.model.ScrapedProduct;

public interface ScrapeUseCase {

    ScrapedProduct getScrapedProduct(String url);
}
