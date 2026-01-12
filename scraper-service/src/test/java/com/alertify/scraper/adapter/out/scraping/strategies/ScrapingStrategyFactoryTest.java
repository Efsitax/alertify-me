package com.alertify.scraper.adapter.out.scraping.strategies;

import com.alertify.common.exception.ScrapeFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class ScrapingStrategyFactoryTest {

    private ScrapingStrategyFactory factory;

    @Mock
    private ScrapingStrategy amazonStrategy;

    @Mock
    private ScrapingStrategy trendyolStrategy;

    @BeforeEach
    public void setUp(){
        factory = new ScrapingStrategyFactory(
                List.of(amazonStrategy, trendyolStrategy)
        );
    }

    @Test
    void should_Return_Correct_Strategy_When_Url_Is_Supported() {
        String url = "https://www.amazon.com.tr/example-product";

        lenient().when(amazonStrategy.canScrape(url)).thenReturn(true);
        lenient().when(trendyolStrategy.canScrape(url)).thenReturn(false);

        ScrapingStrategy result = factory.getStrategy(url);

        assertNotNull(result, "Strategy should not be null");
        assertEquals(amazonStrategy, result, "Returned strategy should be Amazon strategy");
    }

    @Test
    void should_Throw_Exception_When_No_Strategy_Found() {
        String url = "https://www.unkown-site.com/example-product";

        lenient().when(amazonStrategy.canScrape(url)).thenReturn(false);
        lenient().when(trendyolStrategy.canScrape(url)).thenReturn(false);

        assertThrows(
                ScrapeFailedException.class,
                () -> factory.getStrategy(url)
        );
    }
}
