package com.alertify.scraper.adapter.out.scraping.strategies;

import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.Page;

public interface ScrapingStrategy {

    boolean canScrape(String url);
    ScrapedProduct scrape(Page page);
}
