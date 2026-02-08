package com.alertify.scraper.adapter.out.scraping;

import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.scraper.adapter.out.scraping.strategies.ScrapingStrategy;
import com.alertify.scraper.adapter.out.scraping.strategies.ScrapingStrategyFactory;
import com.alertify.scraper.application.port.out.ScraperPort;
import com.alertify.common.exception.ScrapeFailedException;
import com.alertify.scraper.domain.model.ScrapedProduct;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaywrightAdapter implements ScraperPort {

    private final ScrapingStrategyFactory factory;

    @Override
    public ScrapedProduct fetchProduct(String url) {

        ScrapingStrategy strategy = factory.getStrategy(url);

        List<String> args = Arrays.asList(
                "--headless=new",
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-gpu"
        );

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setHeadless(false)
                     .setArgs(args))) {

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080)
                    .setLocale("tr-TR")
                    .setTimezoneId("Europe/Istanbul"));

            context.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            Page page = context.newPage();

            page.route("**/*", route -> {
                String type = route.request().resourceType();
                if (Arrays.asList("image", "media", "font").contains(type)) {
                    route.abort();
                } else {
                    route.resume();
                }
            });

            log.info("Navigating to URL ({}) : {}", strategy.getClass().getSimpleName(), url);
            page.navigate(url, new Page.NavigateOptions().setTimeout(45000));

            return strategy.scrape(page);

        } catch (TimeoutError e) {
            log.error("Playwright Timeout Error for URL: {}", url);
            throw new ScrapeFailedException("Timeout during navigation or scraping: " + e.getMessage());

        } catch (PlaywrightException e) {
            log.error("Playwright General Error: {}", e.getMessage());
            throw new ScrapeFailedException("Playwright unexpected error: " + e.getMessage());

        } catch (ResourceNotFoundException | ScrapeFailedException e) {
            throw e;

        } catch (Exception e) {
            log.error("Unexpected Java Error in PlaywrightAdapter", e);
            throw new ScrapeFailedException("Internal Scraper Error: " + e.getMessage());
        }
    }
}