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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class N11ScrapingStrategyTest extends BaseStrategyTest {

    @InjectMocks
    private N11ScrapingStrategy strategy;

    @Test
    void should_Scrape_Price_From_Meta_Tags_First() {
        when(page.title()).thenReturn("N11 Product");
        mockTextContent("h1.proName", "Laptop");

        // Meta Tag Mock First Priority
        Locator metaLoc = mock(Locator.class);
        when(metaLoc.count()).thenReturn(1);
        when(metaLoc.first()).thenReturn(metaLoc);
        when(metaLoc.getAttribute("content")).thenReturn("25.000,00");

        // first meta tag locator
        when(page.locator("meta[property='product:price:amount']")).thenReturn(metaLoc);

        ScrapedProduct result = strategy.scrape(page);

        assertNotNull(result);
        assertEquals(new BigDecimal("25000.00"), result.getPrice());
        assertEquals("N11", result.getShopName());
    }

    @Test
    void should_Scrape_Price_From_JsonLD_When_Meta_Tags_Missing() {
        when(page.title()).thenReturn("N11");
        mockTextContent("h1.proName", "Mobile Phone");

        // first meta tag is missing
        Locator emptyLoc = mock(Locator.class);
        lenient().when(emptyLoc.count()).thenReturn(0);
        lenient().when(page.locator("meta[property='product:price:amount']")).thenReturn(emptyLoc);

        // second meta tag
        Locator scriptLoc = mock(Locator.class);
        String jsonContent = "{\"@context\": \"...\", \"name\": \"Tel\", \"price\": \"15.000,50\"}";

        when(scriptLoc.first()).thenReturn(scriptLoc);
        when(scriptLoc.textContent()).thenReturn(jsonContent);
        when(page.locator("script[type='application/ld+json']")).thenReturn(scriptLoc);

        ScrapedProduct result = strategy.scrape(page);

        assertEquals(new BigDecimal("15000.50"), result.getPrice());
    }

    @Test
    void should_Scrape_Price_From_HTML_When_Others_Fail() {
        when(page.title()).thenReturn("N11");
        mockTextContent("h1.proName", "Tablet");

        // first meta tag missing
        Locator scriptLoc = mock(Locator.class);
        lenient().when(scriptLoc.first()).thenReturn(scriptLoc);
        lenient().when(scriptLoc.textContent()).thenReturn("");
        lenient().when(page.locator("script[type='application/ld+json']")).thenReturn(scriptLoc);

        // html selector works
        Locator priceLoc = mock(Locator.class);
        when(priceLoc.isVisible()).thenReturn(true);
        when(priceLoc.innerText()).thenReturn("5.000 TL");
        when(priceLoc.first()).thenReturn(priceLoc);

        when(page.locator(".newPrice ins")).thenReturn(priceLoc);

        ScrapedProduct result = strategy.scrape(page);

        assertEquals(new BigDecimal("5000"), result.getPrice());
    }

    @Test
    void should_Detect_Out_Of_Stock_Correctly() {
        when(page.title()).thenReturn("N11");
        mockTextContent("h1.proName", "Tükenmiş Ürün");

        // price selector works
        Locator priceLoc = mock(Locator.class);
        when(priceLoc.isVisible()).thenReturn(true);
        when(priceLoc.innerText()).thenReturn("100 TL");
        when(priceLoc.first()).thenReturn(priceLoc);
        when(page.locator(".newPrice ins")).thenReturn(priceLoc);

        // out of stock indicator
        Locator outOfStockLoc = mock(Locator.class);
        when(outOfStockLoc.isVisible()).thenReturn(true);
        when(page.locator(".outOfStock")).thenReturn(outOfStockLoc);

        ScrapedProduct result = strategy.scrape(page);

        assertFalse(result.getInStock(), "If .outOfStock class is visible, product is out of stock");
    }

    @Test
    void should_Throw_ResourceNotFound_When_404() {
        when(page.title()).thenReturn("Sayfa Bulunamadı - n11.com");
        assertThrows(ResourceNotFoundException.class, () -> strategy.scrape(page));
    }

    @Test
    void should_Throw_ScrapeFailed_When_Title_Wait_Timeout() {
        when(page.title()).thenReturn("Normal Title");
        when(page.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenThrow(new TimeoutError("Timeout"));
        assertThrows(ScrapeFailedException.class, () -> strategy.scrape(page));
    }

    @Test
    void should_Throw_ScrapeFailed_When_InStock_But_Price_Zero() {
        when(page.title()).thenReturn("N11");
        mockTextContent("h1.proName", "Faulty Product");
        assertThrows(ScrapeFailedException.class, () -> strategy.scrape(page));
    }
}