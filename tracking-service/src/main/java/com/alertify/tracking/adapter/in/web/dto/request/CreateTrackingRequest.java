package com.alertify.tracking.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTrackingRequest(
        @NotBlank(message = "URL cannot be blank")
        String url,

        @NotNull(message = "Target price cannot be null")
        @Positive(message = "Target price must be bigger than zero")
        BigDecimal targetPrice
) {
}
