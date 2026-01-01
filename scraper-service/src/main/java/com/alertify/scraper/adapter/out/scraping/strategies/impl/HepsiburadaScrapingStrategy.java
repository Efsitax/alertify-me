package com.alertify.scraper.adapter.out.scraping.strategies.impl;

import com.alertify.scraper.adapter.out.scraping.strategies.ScrapingStrategy;
import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Paths;

@Component
public class HepsiburadaScrapingStrategy implements ScrapingStrategy {

    @Override
    public boolean canScrape(String url) {
        return url.contains("hepsiburada.com");
    }

    @Override
    public ScrapedProduct scrape(Page page) {
        try {
            page.waitForSelector("h1", new Page.WaitForSelectorOptions().setTimeout(15000));
        } catch (Exception e) {
            try {
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get("hata-hepsiburada.png")).setFullPage(true));
            } catch (Exception ex) {
                // error
            }
        }

        String productName = "Name not found";
        if (page.locator("h1[data-test-id='title']").isVisible()) {
            productName = page.locator("h1[data-test-id='title']").innerText();
        } else if (page.locator("h1").isVisible()) {
            productName = page.locator("h1").first().innerText();
        }

        BigDecimal price = tryCustomSelectors(page);

        return ScrapedProduct.builder()
                .productName(productName.trim())
                .price(price)
                .currency("TRY")
                .shopName("Hepsiburada")
                .inStock(true)
                .build();
    }

    private BigDecimal tryCustomSelectors(Page page) {
        String[] selectors = {
                "[data-test-id='checkout-price'] div:nth-child(2)",
                "[data-test-id='non-premium-price'] b",
                "[data-test-id='default-price'] span",
                "[data-test-id='price-current-price']"
        };

        for (String selector : selectors) {
            if (page.locator(selector).first().isVisible()) {
                String rawText = page.locator(selector).first().innerText();
                return parsePrice(rawText);
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null) return BigDecimal.ZERO;
        try {
            String cleanPrice = rawPrice.split("\n")[0]
                    .replace("TL", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();
            cleanPrice = cleanPrice.replaceAll("[^0-9.]", "");
            return new BigDecimal(cleanPrice);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}