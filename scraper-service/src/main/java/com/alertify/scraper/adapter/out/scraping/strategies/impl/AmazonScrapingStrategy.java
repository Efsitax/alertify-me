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
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmazonScrapingStrategy implements ScrapingStrategy {

    @Override
    public boolean canScrape(String url) {
        return url.contains("amazon");
    }

    @Override
    public ScrapedProduct scrape(Page page) {

        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            if (page.locator("from[action*='/errors/validateCaptcha']").isVisible() ||
                    page.title().contains("Robot Check")) {
                throw new ScrapeFailedException("Amazon detected bot/captcha. Retry required.");
            }

            if (page.title().contains("Page Not Found") ||
                    page.locator("img[alt*='Dogs of Amazon']").isVisible()) {
                throw new ResourceNotFoundException("Amazon Product", "page", "Page not found / Dogs");
            }
            page.waitForSelector("span#productTitle", new Page.WaitForSelectorOptions().setTimeout(10000));
        } catch (TimeoutError e) {
            throw new ScrapeFailedException("Timeout waiting for product title. Network might be slow.");
        }

        String productName;
        if (page.locator("span#productTitle").isVisible()) {
            productName = page.locator("span#productTitle").innerText();
        } else {
            throw new ScrapeFailedException("Product title selector not visible. HTML structure might have changed.");
        }

        BigDecimal price = BigDecimal.ZERO;

        Locator priceContainer = page.locator(".a-price.priceToPay").first();

        if (!priceContainer.isVisible()) {
            priceContainer = page.locator("#corePriceDisplay_desktop_feature_div .a-price").first();
        }
        if (!priceContainer.isVisible()) {
            priceContainer = page.locator(".a-price").first();
        }

        boolean inStock = true;
        if (page.locator("#availability").isVisible()) {
            String text = page.locator("#availability").innerText().toLowerCase();
            if (text.contains("unavailable") || text.contains("stokta yok") || text.contains("mevcut değil")) {
                inStock = false;
            }
        }

        if (priceContainer.isVisible()) {
            try {
                String whole = "0";
                if (priceContainer.locator(".a-price-whole").count() > 0) {
                    String rawWhole = priceContainer.locator(".a-price-whole").first().innerText();
                    whole = rawWhole.replaceAll("[^0-9]", "");
                }

                String fraction = "00";
                if (priceContainer.locator(".a-price-fraction").count() > 0) {
                    String rawFraction = priceContainer.locator(".a-price-fraction").first().innerText();
                    fraction = rawFraction.replaceAll("[^0-9]", "");
                }

                String finalPriceString = whole + "." + fraction;
                price = new BigDecimal(finalPriceString);

            } catch (Exception e) {
                throw new ScrapeFailedException("Failed to parse price text. Format changed?");
            }
        } else {
            if (inStock) {
                throw new ScrapeFailedException("Product is in stock but price selector not found.");
            }
        }

        return ScrapedProduct.builder()
                .productName(productName.trim())
                .price(price)
                .currency("TRY")
                .shopName("Amazon")
                .inStock(inStock)
                .build();
    }
}