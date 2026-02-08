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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
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
            String title = page.title().toLowerCase();
            if (title.contains("sayfa bulunamadı") || title.contains("aradığınız sayfayı bulamadık")) {
                throw new ResourceNotFoundException("Trendyol Product", "page", "Page Not Found");
            }

            try {
                page.waitForSelector("h1", new Page.WaitForSelectorOptions().setTimeout(15000));
            } catch (TimeoutError e) {
                throw new ScrapeFailedException("Timeout waiting for Trendyol product title. Network or blocking issue.");
            }

            String productName;
            if (page.locator("h1.pr-new-br").isVisible()) {
                productName = page.locator("h1.pr-new-br").first().innerText(); // Marka + İsim
            } else if (page.locator("h1").isVisible()) {
                productName = page.locator("h1").first().innerText();
            } else {
                throw new ScrapeFailedException("Product title selector not visible on Trendyol.");
            }

            boolean inStock = !page.locator(".sold-out-icon").isVisible() &&
                    !page.locator("text='Tükendi'").isVisible() &&
                    !page.locator("button:has-text('Gelince Haber Ver')").isVisible();

            BigDecimal price = tryCssSelectors(page.locator(".price-wrapper").first());

            if (inStock && price.compareTo(BigDecimal.ZERO) == 0) {
                throw new ScrapeFailedException("Product is in stock but price could not be parsed.");
            }

            return ScrapedProduct.builder()
                    .productName(productName)
                    .price(price)
                    .currency("TRY")
                    .shopName("Trendyol")
                    .inStock(inStock)
                    .build();

        } catch (ResourceNotFoundException | ScrapeFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new ScrapeFailedException("Unexpected error during Trendyol scraping: " + e.getMessage());
        }
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
