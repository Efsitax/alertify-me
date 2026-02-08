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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class N11ScrapingStrategy implements ScrapingStrategy {

    @Override
    public boolean canScrape(
            String url
    ) {
        return url.contains("n11.com");
    }

    @Override
    public ScrapedProduct scrape(
            Page page
    ) {

        String title = page.title().toLowerCase();
        if (title.contains("sayfa bulunamadı") || title.contains("404")) {
            throw new ResourceNotFoundException("N11 Product", "page", "Page Not Found");
        }

        try {
            page.waitForSelector("h1", new Page.WaitForSelectorOptions().setTimeout(15000));
        } catch (TimeoutError e) {
            throw new ScrapeFailedException("Timeout waiting for N11 product title. Verification/Captcha required?");
        }

        String productName;
        if (page.locator("h1.proName").isVisible()) {
            productName = page.locator("h1.proName").first().innerText();
        } else if (page.locator("h1.title").isVisible()) {
            productName = page.locator("h1.title").first().innerText();
        } else if (page.locator("h1").isVisible()) {
            productName = page.locator("h1").first().innerText();
        } else {
            throw new ScrapeFailedException("Product title selector not visible on N11.");
        }

        boolean inStock = !page.locator(".outOfStock").isVisible() &&
                !page.locator("text='Tükendi'").isVisible() &&
                !page.locator("a.btn-grey").isVisible();

        BigDecimal price = findPriceGuaranteed(page);

        if (inStock && price.compareTo(BigDecimal.ZERO) == 0) {
            throw new ScrapeFailedException("Product is in stock but price could not be parsed.");
        }

        return ScrapedProduct.builder()
                .productName(productName.trim())
                .price(price)
                .currency("TRY")
                .shopName("N11")
                .inStock(inStock)
                .build();
    }

    private BigDecimal findPriceGuaranteed(
            Page page
    ) {

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
            log.warn("JSON-LD parsing failed for N11: {}", e.getMessage());
        }

        Locator priceContainer = page.locator(".newPrice ins").first();

        if (!priceContainer.isVisible()) {
            priceContainer = page.locator(".priceContainer .newPrice").first();
        }

        if (priceContainer.isVisible()) {
            String text = priceContainer.innerText().trim();
            if (!text.isEmpty()) {
                return parsePrice(text);
            }
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal parsePrice(
            String rawPrice
    ) {

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