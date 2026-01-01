package com.alertify.scraper.adapter.out.scraping.strategies.impl;

import com.alertify.scraper.adapter.out.scraping.strategies.ScrapingStrategy;
import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TrendyolScrapingStrategy implements ScrapingStrategy {

    @Override
    public boolean canScrape(String url) {
        return url.contains("trendyol.com");
    }

    @Override
    public ScrapedProduct scrape(Page page) {
        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (Exception e) {
            // ignore
        }

        String productName = "Name not found";
        if (page.locator("h1").count() > 0) {
            productName = page.locator("h1").first().textContent();
        }

        BigDecimal price = tryCssSelectors(page.locator(".price-wrapper").first());

        return ScrapedProduct.builder()
                .productName(productName)
                .price(price)
                .currency("TRY")
                .shopName("Trendyol")
                .inStock(true)
                .build();
    }

    private BigDecimal tryCssSelectors(Locator priceWrapper) {
        String[] selectors = {
                ".discounted",
                ".ty-plus-price-discounted-price",
                ".new-price"
        };

        for (String selector : selectors) {
            if (priceWrapper.locator(selector).count() > 0) {
                String rawText = priceWrapper.locator(selector).first().textContent();
                BigDecimal result = parsePrice(rawText);
                if (result.compareTo(BigDecimal.ZERO) > 0) {
                    return result;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal parsePrice(String rawPrice) {
        String cleanPrice = rawPrice
                .replace("TL", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        return new BigDecimal(cleanPrice);
    }
}
