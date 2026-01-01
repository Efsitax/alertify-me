package com.alertify.scraper.adapter.out.scraping.strategies.impl;

import com.alertify.scraper.adapter.out.scraping.strategies.ScrapingStrategy;
import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class N11ScrapingStrategy implements ScrapingStrategy {

    @Override
    public boolean canScrape(String url) {
        return url.contains("n11.com");
    }

    @Override
    public ScrapedProduct scrape(Page page) {
        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (Exception e) {
            // ignore
        }

        String productName = "Name not found";
        if (page.locator("h1.title").isVisible()) {
            productName = page.locator("h1.title").first().innerText();
        } else if (page.locator("h1.proName").isVisible()) {
            productName = page.locator("h1.proName").first().innerText();
        } else if (page.locator("h1").isVisible()) {
            productName = page.locator("h1").first().innerText();
        }

        BigDecimal price = findPriceGuaranteed(page);

        return ScrapedProduct.builder()
                .productName(productName.trim())
                .price(price)
                .currency("TRY")
                .shopName("N11")
                .inStock(true)
                .build();
    }

    private BigDecimal findPriceGuaranteed(Page page) {

        String[] metaSelectors = {
                "meta[property='product:price:amount']",
                "meta[property='og:price:amount']",
                "meta[name='twitter:data1']"
        };

        for (String meta : metaSelectors) {
            if (page.locator(meta).count() > 0) {
                String content = page.locator(meta).first().getAttribute("content");
                if (content != null && !content.isEmpty()) {
                    return parsePrice(content);
                }
            }
        }

        try {
            String scriptContent = page.locator("script[type='application/ld+json']").first().textContent();
            if (scriptContent.contains("\"price\"")) {
                Pattern p = Pattern.compile("\"price\"\\s*:\\s*\"([0-9.,]+)\"");
                Matcher m = p.matcher(scriptContent);
                if (m.find()) {
                    return parsePrice(m.group(1));
                }
            }
        } catch (Exception e) {
            // ignore
        }

        Locator priceContainer = page.locator(".newPrice ins").first();

        for (int i = 0; i < 10; i++) {
            if (priceContainer.isVisible()) {
                String text = priceContainer.innerText().trim();
                if (!text.isEmpty()) {
                    return parsePrice(text);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null) return BigDecimal.ZERO;
        try {
            String cleanPrice = rawPrice
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