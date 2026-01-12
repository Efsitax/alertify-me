package com.alertify.scraper.adapter.out.scraping.strategies;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public abstract class BaseStrategyTest {

    @Mock
    protected Page page;

    @Mock
    protected Locator locator;

    @BeforeEach
    void setUpBase() {
        lenient().when(page.locator(anyString())).thenReturn(locator);

        lenient().when(locator.locator(anyString())).thenReturn(locator);
        lenient().when(locator.first()).thenReturn(locator);
        lenient().when(locator.isVisible()).thenReturn(false);

        lenient().when(page.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class))).thenReturn(null);
    }

    @SuppressWarnings("SameParameterValue")
    protected void mockTextContent(String selector, String text) {
        Locator specificLocator = mock(Locator.class);

        lenient().when(page.locator(selector)).thenReturn(specificLocator);

        lenient().when(specificLocator.first()).thenReturn(specificLocator);
        lenient().when(specificLocator.isVisible()).thenReturn(true);
        lenient().when(specificLocator.innerText()).thenReturn(text);
        lenient().when(specificLocator.textContent()).thenReturn(text);
        lenient().when(specificLocator.getAttribute("content")).thenReturn(text);
    }
}
