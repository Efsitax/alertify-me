package com.alertify.scraper.application.service;

import com.alertify.scraper.application.port.in.ScrapeUseCase;
import com.alertify.scraper.application.port.out.ScraperPort;
import com.alertify.scraper.domain.model.ScrapedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScraperService implements ScrapeUseCase {

    private final ScraperPort port;

    @Override
    public ScrapedProduct getScrapedProduct(String url) {
        return port.fetchProduct(url);
    }
}
