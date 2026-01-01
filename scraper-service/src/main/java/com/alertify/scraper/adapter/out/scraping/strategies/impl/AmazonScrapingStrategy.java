package com.alertify.scraper.adapter.out.scraping.strategies.impl;

import com.alertify.scraper.adapter.out.scraping.strategies.ScrapingStrategy;
import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
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
            page.waitForSelector("span#productTitle", new Page.WaitForSelectorOptions().setTimeout(10000));
        } catch (Exception e) {
            // ignore
        }

        String productName = "Name not found";
        if (page.locator("span#productTitle").isVisible()) {
            productName = page.locator("span#productTitle").innerText();
        }

        BigDecimal price = BigDecimal.ZERO;

        Locator priceContainer = page.locator(".a-price.priceToPay").first();

        if (!priceContainer.isVisible()) {
            priceContainer = page.locator("#corePriceDisplay_desktop_feature_div .a-price").first();
        }

        if (!priceContainer.isVisible()) {
            priceContainer = page.locator(".a-price").first();
        }

        if (priceContainer.isVisible()) {
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
        }

        boolean inStock = true;
        if (page.locator("#availability").isVisible()) {
            String text = page.locator("#availability").innerText().toLowerCase();
            if (text.contains("unavailable") || text.contains("stokta yok") || text.contains("mevcut değil")) {
                inStock = false;
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