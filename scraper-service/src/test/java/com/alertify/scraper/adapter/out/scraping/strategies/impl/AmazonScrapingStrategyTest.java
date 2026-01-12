package com.alertify.scraper.adapter.out.scraping.strategies.impl;

import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.common.exception.ScrapeFailedException;
import com.alertify.scraper.adapter.out.scraping.strategies.BaseStrategyTest;
import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AmazonScrapingStrategyTest extends BaseStrategyTest {

    @InjectMocks
    private AmazonScrapingStrategy strategy;

    @Test
    void should_Scrape_Correctly_When_Product_Exists() {
        // mocking page title
        String productName = "iPhone 15 128GB";
        when(page.title()).thenReturn("Amazon.com.tr: iPhone 15");

        mockTextContent("span#productTitle", productName);

        // mocking price locators
        Locator priceLocator = mock(Locator.class);
        when(priceLocator.first()).thenReturn(priceLocator);
        when(priceLocator.isVisible()).thenReturn(true);
        when(page.locator(".a-price.priceToPay")).thenReturn(priceLocator);

        Locator wholeLoc = mock(Locator.class);
        Locator fracLoc = mock(Locator.class);

        when(priceLocator.locator(".a-price-whole")).thenReturn(wholeLoc);
        when(priceLocator.locator(".a-price-fraction")).thenReturn(fracLoc);

        when(wholeLoc.count()).thenReturn(1);
        when(wholeLoc.first()).thenReturn(wholeLoc);
        when(wholeLoc.innerText()).thenReturn("50.000");

        when(fracLoc.count()).thenReturn(1);
        when(fracLoc.first()).thenReturn(fracLoc);
        when(fracLoc.innerText()).thenReturn("00");

        ScrapedProduct result = strategy.scrape(page);

        // assertions
        assertNotNull(result, "ScrapedProduct should not be null");
        assertEquals("iPhone 15 128GB", result.getProductName());
        assertEquals(new BigDecimal("50000.00"), result.getPrice());
        assertEquals("Amazon", result.getShopName());
        assertTrue(result.getInStock(), "Product should be in stock");
    }

    @Test
    void should_Detect_When_Product_Is_Out_Of_Stock() {
        when(page.title()).thenReturn("Amazon Product");
        mockTextContent("span#productTitle", "PlayStation 5");

        // Mock Price
        Locator priceContainer = mock(Locator.class);
        when(priceContainer.first()).thenReturn(priceContainer);
        when(priceContainer.isVisible()).thenReturn(true);
        when(page.locator(".a-price.priceToPay")).thenReturn(priceContainer);

        // Simulate missing price parts
        Locator wholeLoc = mock(Locator.class);
        when(priceContainer.locator(".a-price-whole")).thenReturn(wholeLoc);
        when(wholeLoc.count()).thenReturn(0);

        Locator fracLoc = mock(Locator.class);
        when(priceContainer.locator(".a-price-fraction")).thenReturn(fracLoc);
        when(fracLoc.count()).thenReturn(0);

        // mocking availability
        Locator availabilityLoc = mock(Locator.class);
        when(page.locator("#availability")).thenReturn(availabilityLoc);
        when(availabilityLoc.isVisible()).thenReturn(true);

        when(availabilityLoc.innerText()).thenReturn("Şu anda stokta yok.");

        ScrapedProduct result = strategy.scrape(page);

        assertFalse(result.getInStock(), "Should be false when text contains 'stokta yok'");
        assertEquals(new BigDecimal("0.00"), result.getPrice());
    }

    @Test
    void should_Throw_ResourceNotFound_When_Dogs_Image_Visible() {
        when(page.title()).thenReturn("Amazon");

        // Mock "Dogs of Amazon" image being visible
        Locator dogsImage = mock(Locator.class);
        when(dogsImage.isVisible()).thenReturn(true);
        when(page.locator("img[alt*='Dogs of Amazon']")).thenReturn(dogsImage);

        assertThrows(ResourceNotFoundException.class, () -> strategy.scrape(page));
    }

    @Test
    void should_Throw_ScrapeFailed_When_Captcha_Form_Visible() {
        lenient().when(page.title()).thenReturn("Amazon");

        // Mock Captcha form
        Locator captchaForm = mock(Locator.class);
        when(captchaForm.isVisible()).thenReturn(true);
        when(page.locator("from[action*='/errors/validateCaptcha']")).thenReturn(captchaForm);

        assertThrows(ScrapeFailedException.class, () -> strategy.scrape(page));
    }
}
