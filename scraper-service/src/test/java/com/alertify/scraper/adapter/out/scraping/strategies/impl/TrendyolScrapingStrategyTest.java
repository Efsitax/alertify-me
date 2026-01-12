package com.alertify.scraper.adapter.out.scraping.strategies.impl;

import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.common.exception.ScrapeFailedException;
import com.alertify.scraper.adapter.out.scraping.strategies.BaseStrategyTest;
import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrendyolScrapingStrategyTest extends BaseStrategyTest {

    @InjectMocks
    private TrendyolScrapingStrategy strategy;

    @Test
    void should_Scrape_Correctly_When_Product_In_Stock() {
        // mocking page title
        when(page.title()).thenReturn("Trendyol: Mavi Kazak");
        mockTextContent("h1.pr-new-br", "Mavi Jeans Erkek Kazak");

        // mocking price
        Locator priceWrapper = mock(Locator.class);
        when(page.locator(".price-wrapper")).thenReturn(priceWrapper);
        when(priceWrapper.first()).thenReturn(priceWrapper);

        Locator discountedPrice = mock(Locator.class);
        when(priceWrapper.locator(".discounted")).thenReturn(discountedPrice);

        when(discountedPrice.count()).thenReturn(1);
        when(discountedPrice.first()).thenReturn(discountedPrice);
        when(discountedPrice.textContent()).thenReturn("299,99 TL");

        ScrapedProduct result = strategy.scrape(page);

        // assertions
        assertNotNull(result, "ScrapedProduct should not be null");
        assertEquals("Mavi Jeans Erkek Kazak", result.getProductName(), "Product name should match");
        assertEquals("Trendyol", result.getShopName(), "Shop name should be Trendyol");
        assertTrue(result.getInStock());
    }

    @Test
    void should_Detect_Out_Of_Stock_When_Button_Visible() {
        // mocking page title
        when(page.title()).thenReturn("Trendyol Ürün");
        mockTextContent("h1.pr-new-br", "Tükenmiş Ürün");

        // mocking price
        Locator priceWrapper = mock(Locator.class);
        when(page.locator(".price-wrapper")).thenReturn(priceWrapper);
        when(priceWrapper.first()).thenReturn(priceWrapper);

        Locator priceLoc = mock(Locator.class);
        when(priceWrapper.locator(".discounted")).thenReturn(priceLoc);

        when(priceLoc.count()).thenReturn(1);
        when(priceLoc.first()).thenReturn(priceLoc);
        when(priceLoc.textContent()).thenReturn("500 TL");

        // mocking out-of-stock button
        Locator outOfStockBtn = mock(Locator.class);
        when(outOfStockBtn.isVisible()).thenReturn(true);
        when(page.locator("button:has-text('Gelince Haber Ver')")).thenReturn(outOfStockBtn);

        ScrapedProduct result = strategy.scrape(page);

        assertFalse(result.getInStock(), "That button means product is out of stock");
    }

    @Test
    void should_Throw_ResourceNotFound_When_Page_Not_Found() {
        when(page.title()).thenReturn("Sayfa Bulunamadı - Trendyol");
        assertThrows(ResourceNotFoundException.class, () -> strategy.scrape(page));
    }

    @Test
    void should_Throw_ScrapeFailed_When_Timeout_Occurs() {
        when(page.title()).thenReturn("Standard Title");

        // simulating timeout on waitForSelector
        when(page.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenThrow(new TimeoutError("Timeout occurred"));
        assertThrows(ScrapeFailedException.class, () -> strategy.scrape(page));
    }

    @Test
    void should_Parse_Messy_Price_Format_Correctly() {
        when(page.title()).thenReturn("Test");
        mockTextContent("h1.pr-new-br", "Test Product");

        Locator priceWrapper = mock(Locator.class);
        when(page.locator(".price-wrapper")).thenReturn(priceWrapper);
        when(priceWrapper.first()).thenReturn(priceWrapper);

        // Price format is messy
        Locator priceLoc = mock(Locator.class);
        when(priceWrapper.locator(".discounted")).thenReturn(priceLoc);
        when(priceLoc.count()).thenReturn(1);
        when(priceLoc.first()).thenReturn(priceLoc);
        when(priceLoc.textContent()).thenReturn(" 1.250,00 TL ");

        ScrapedProduct result = strategy.scrape(page);

        assertEquals(new BigDecimal("1250.00"), result.getPrice());
    }
}
