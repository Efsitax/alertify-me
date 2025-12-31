package com.alertify.tracking.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateTrackingRequest(
        @Positive(message = "Target price must be bigger than zero")
        BigDecimal targetPrice,

        @NotNull(message = "Active status cannot be null")
        Boolean isActive
) {
}
