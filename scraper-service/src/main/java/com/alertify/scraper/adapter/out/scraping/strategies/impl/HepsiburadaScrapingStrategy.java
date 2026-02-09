/*
 * Copyright 2026 efsitax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alertify.scraper.adapter.out.scraping.strategies.impl;

import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.common.exception.ScrapeFailedException;
import com.alertify.scraper.adapter.out.scraping.strategies.ScrapingStrategy;
import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class HepsiburadaScrapingStrategy implements ScrapingStrategy {

    @Override
    public boolean canScrape(
            String url
    ) {
        return url.contains("hepsiburada.com");
    }

    @Override
    public ScrapedProduct scrape(
            Page page
    ) {

        try {
            String title = page.title().toLowerCase();
            if (title.contains("sayfa bulunamadı") || title.contains("böyle bir ürün yok")) {
                throw new ResourceNotFoundException("Hepsiburada Product", "page", "Page Not Found");
            }
            try {
                page.waitForSelector("h1", new Page.WaitForSelectorOptions().setTimeout(15000));
            } catch (TimeoutError e) {
                throw new ScrapeFailedException("Timeout waiting for product title. Site might be slow or blocking.", true);
            }

            String productName;
            if (page.locator("h1[data-test-id='title']").isVisible()) {
                productName = page.locator("h1[data-test-id='title']").innerText();
            } else if (page.locator("h1").isVisible()) {
                productName = page.locator("h1").first().innerText();
            } else {
                throw new ScrapeFailedException("Product title selector not visible.", false);
            }

            boolean inStock = !page.locator("[data-test-id='out-of-stock-button']").isVisible() &&
                    !page.locator("button:has-text('Gelince Haber Ver')").isVisible();

            BigDecimal price = tryCustomSelectors(page);

            if (inStock && price.compareTo(BigDecimal.ZERO) == 0) {
                throw new ScrapeFailedException("Product is in stock but price could not be parsed.", false);
            }

            return ScrapedProduct.builder()
                    .productName(productName.trim())
                    .price(price)
                    .currency("TRY")
                    .shopName("Hepsiburada")
                    .inStock(true)
                    .build();
        } catch (ResourceNotFoundException | ScrapeFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapeFailedException("Unexpected error during Hepsiburada scraping: " + e.getMessage(), false);
        }
    }

    private BigDecimal tryCustomSelectors(
            Page page
    ) {

        String[] selectors = {
                "[data-test-id='checkout-price'] div:nth-child(2)",
                "[data-test-id='non-premium-price'] b",
                "[data-test-id='default-price'] span",
                "[data-test-id='price-current-price']"
        };

        for (String selector : selectors) {
            if (page.locator(selector).first().isVisible()) {
                try {
                    String rawText = page.locator(selector).first().innerText();
                    BigDecimal parsedPrice = parsePrice(rawText);
                    if (parsedPrice.compareTo(BigDecimal.ZERO) > 0) {
                        return parsedPrice;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse price with selector: {}", selector);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal parsePrice(
            String rawPrice
    ) {

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