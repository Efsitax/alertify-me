package com.alertify.tracking.application.port.out;

import java.util.UUID;

public interface ScrapePort {

    void sendScrapeRequest(UUID productId, String url);
}
