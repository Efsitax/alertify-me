package com.alertify.scraper.application.port.out;

import com.alertify.scraper.domain.model.ScrapedProduct;

public interface ScraperPort {

    ScrapedProduct fetchProduct(String url);
}
