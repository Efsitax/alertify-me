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
