package com.alertify.scraper.adapter.in.web.controller;

import com.alertify.scraper.application.port.in.ScrapeUseCase;
import com.alertify.scraper.domain.model.ScrapedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScraperController {

    private final ScrapeUseCase useCase;

    @GetMapping("/api/v1/scrape")
    public ResponseEntity<ScrapedProduct> scrapeProduct(@RequestParam String url) {
        ScrapedProduct product = useCase.getScrapedProduct(url);
        return ResponseEntity.ok(product);
    }
}
