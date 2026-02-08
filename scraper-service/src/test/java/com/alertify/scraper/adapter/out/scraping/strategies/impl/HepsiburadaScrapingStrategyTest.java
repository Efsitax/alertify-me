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
class HepsiburadaScrapingStrategyTest extends BaseStrategyTest {

    @InjectMocks
    private HepsiburadaScrapingStrategy strategy;

    @Test
    void should_Scrape_Correctly_When_Primary_Price_Selector_Works() {

        // mocking page title
        when(page.title()).thenReturn("Hepsiburada: Laptop");

        Locator titleLoc = mock(Locator.class);
        when(titleLoc.isVisible()).thenReturn(true);
        when(titleLoc.innerText()).thenReturn("Asus Gaming Laptop");
        when(page.locator("h1[data-test-id='title']")).thenReturn(titleLoc);

        // mocking price
        Locator priceLoc = mock(Locator.class);
        when(priceLoc.first()).thenReturn(priceLoc);
        when(priceLoc.isVisible()).thenReturn(true);
        when(priceLoc.innerText()).thenReturn("25.000 TL");

        when(page.locator("[data-test-id='checkout-price'] div:nth-child(2)")).thenReturn(priceLoc);

        ScrapedProduct result = strategy.scrape(page);

        assertNotNull(result);
        assertEquals("Asus Gaming Laptop", result.getProductName());
        assertEquals(new BigDecimal("25000"), result.getPrice());
        assertEquals("Hepsiburada", result.getShopName());
    }

    @Test
    void should_Scrape_Using_Fallback_Selector_When_Primary_Fails() {

        when(page.title()).thenReturn("HB Ürün");

        Locator mainTitleLoc = mock(Locator.class);
        lenient().when(mainTitleLoc.isVisible()).thenReturn(false);
        when(page.locator("h1[data-test-id='title']")).thenReturn(mainTitleLoc);

        // Fallback Title Selector works
        Locator fallbackTitleLoc = mock(Locator.class);
        when(fallbackTitleLoc.first()).thenReturn(fallbackTitleLoc);
        when(fallbackTitleLoc.isVisible()).thenReturn(true);
        when(fallbackTitleLoc.innerText()).thenReturn("Secondary Title");
        when(page.locator("h1")).thenReturn(fallbackTitleLoc);


        // primary price selector FAILS
        Locator priceLoc1 = mock(Locator.class);
        lenient().when(priceLoc1.first()).thenReturn(priceLoc1);
        lenient().when(priceLoc1.isVisible()).thenReturn(false);
        lenient().when(page.locator("[data-test-id='checkout-price'] div:nth-child(2)")).thenReturn(priceLoc1);

        // secondary price selector works
        Locator priceLoc2 = mock(Locator.class);
        when(priceLoc2.first()).thenReturn(priceLoc2);
        when(priceLoc2.isVisible()).thenReturn(true);
        when(priceLoc2.innerText()).thenReturn("10.500,50 TL");
        when(page.locator("[data-test-id='non-premium-price'] b")).thenReturn(priceLoc2);

        ScrapedProduct result = strategy.scrape(page);

        assertEquals("Secondary Title", result.getProductName());
        assertEquals(new BigDecimal("10500.50"), result.getPrice());
    }

    @Test
    void should_Throw_ResourceNotFound_When_Page_Is_404() {

        when(page.title()).thenReturn("Sayfa Bulunamadı - Hepsiburada");
        assertThrows(ResourceNotFoundException.class, () -> strategy.scrape(page));
    }

    @Test
    void should_Throw_ScrapeFailed_When_Timeout_Occurs() {

        when(page.title()).thenReturn("Normal Title");

        // simulate Timeout
        when(page.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenThrow(new TimeoutError("Timeout!"));
        assertThrows(ScrapeFailedException.class, () -> strategy.scrape(page));
    }

    @Test
    void should_Handle_Messy_Price_Format() {

        when(page.title()).thenReturn("HB");
        mockTextContent("h1[data-test-id='title']", "Test Product");

        // messy Price format
        Locator priceLoc = mock(Locator.class);
        when(priceLoc.first()).thenReturn(priceLoc);
        when(priceLoc.isVisible()).thenReturn(true);
        when(priceLoc.innerText()).thenReturn(" 1.299,90 TL \n (KDV Dahil) ");

        when(page.locator("[data-test-id='checkout-price'] div:nth-child(2)")).thenReturn(priceLoc);

        ScrapedProduct result = strategy.scrape(page);
        assertEquals(new BigDecimal("1299.90"), result.getPrice());
    }

    @Test
    void should_Throw_Exception_When_InStock_But_Price_Zero() {

        when(page.title()).thenReturn("HB");
        mockTextContent("h1[data-test-id='title']", "Stokta Var Ama Fiyat Yok");
        assertThrows(ScrapeFailedException.class, () -> strategy.scrape(page),
                "Should throw exception if product is in stock but price is 0");
    }
}