package com.alertify.scraper.adapter.out.scraping.strategies;

import com.alertify.common.exception.ScrapeFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScrapingStrategyFactory {

    private final List<ScrapingStrategy> strategies;

    public ScrapingStrategy getStrategy(String url) {
        return strategies.stream()
                .filter(strategy -> strategy.canScrape(url))
                .findFirst()
                .orElseThrow(() -> new ScrapeFailedException("No scraping strategy found for URL: " + url));
    }
}
