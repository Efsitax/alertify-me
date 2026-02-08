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
