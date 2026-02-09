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

package com.alertify.tracking.adapter.in.web.controller;

import com.alertify.common.exception.AccessDeniedException;
import com.alertify.common.exception.ResourceAlreadyExistsException;
import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.common.rest.GlobalExceptionHandler;
import com.alertify.tracking.adapter.in.web.dto.request.CreateTrackingRequest;
import com.alertify.tracking.adapter.in.web.dto.request.UpdateTrackingRequest;
import com.alertify.tracking.application.port.in.TrackingUseCase;
import com.alertify.tracking.domain.model.PriceHistory;
import com.alertify.tracking.domain.model.TrackedProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrackingController.class)
@Import(GlobalExceptionHandler.class)
public class TrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrackingUseCase trackingUseCase;

    @Test
    void shouldReturnCreated_WhenRequestIsValid() throws Exception {

        UUID userId = UUID.randomUUID();

        CreateTrackingRequest request = new CreateTrackingRequest(
                "https://test.com/",
                BigDecimal.valueOf(150)
        );

        TrackedProduct mockProduct = TrackedProduct.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .url("https://test.com/")
                .targetPrice(BigDecimal.valueOf(150))
                .isActive(true)
                .build();

        when(trackingUseCase.createTrackedProduct(
                eq(userId),
                eq(request.url()),
                eq(request.targetPrice())
        )).thenReturn(mockProduct);

        mockMvc.perform(post("/api/v1/trackings")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://test.com/"))
                .andExpect(jsonPath("$.targetPrice").value(150));
    }

    @Test
    void shouldReturnBadRequest_WhenCreateRequestHasInvalidData() throws Exception {

        UUID userId = UUID.randomUUID();

        CreateTrackingRequest request = new CreateTrackingRequest("", BigDecimal.valueOf(-1));

        mockMvc.perform(post("/api/v1/trackings")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorReason").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnOk_WhenGettingTrackedProducts() throws Exception {

        UUID userId = UUID.randomUUID();

        List<TrackedProduct> productList = List.of(
                TrackedProduct.builder().id(UUID.randomUUID()).userId(userId).url("url1").build(),
                TrackedProduct.builder().id(UUID.randomUUID()).userId(userId).url("url2").build()
        );

        when(trackingUseCase.getTrackedProducts(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(productList));

        mockMvc.perform(get("/api/v1/trackings/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].url").value("url1"));
    }

    @Test
    void shouldReturnBadRequest_WhenPriceIsNegative() throws Exception {

        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateTrackingRequest request = new UpdateTrackingRequest(
                BigDecimal.valueOf(-10),
                true
        );

        mockMvc.perform(put("/api/v1/trackings/{productId}", productId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorReason").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnNotFound_WhenProductDoesNotExist() throws Exception {

        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateTrackingRequest request = new UpdateTrackingRequest(
                BigDecimal.valueOf(100),
                true
        );

        when(trackingUseCase.updateTrackedProduct(
                eq(userId),
                eq(productId),
                any(BigDecimal.class),
                any(Boolean.class)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Tracked Product",
                        "productId",
                        productId.toString()
                )
        );

        mockMvc.perform(put("/api/v1/trackings/{productId}", productId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorReason").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnBadRequest_WhenUpdatingOthersProduct() throws Exception {

        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateTrackingRequest request = new UpdateTrackingRequest(
                BigDecimal.valueOf(100),
                true
        );

        when(trackingUseCase.updateTrackedProduct(
                eq(userId),
                eq(productId),
                any(BigDecimal.class),
                any(Boolean.class)
        )).thenThrow(new AccessDeniedException("You do not have permission to update this tracked product."));

        mockMvc.perform(put("/api/v1/trackings/{productId}", productId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorReason").value("ACCESS_DENIED"));
    }

    @Test
    void shouldReturnOk_WhenRequestIsValid() throws Exception {

        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateTrackingRequest request = new UpdateTrackingRequest(
                BigDecimal.valueOf(100),
                true
        );

        TrackedProduct mockProduct = TrackedProduct.builder()
                .id(productId)
                .userId(userId)
                .url("https://test.com")
                .targetPrice(BigDecimal.valueOf(100))
                .isActive(true)
                .build();

        when(trackingUseCase.updateTrackedProduct(
                eq(userId),
                eq(productId),
                any(BigDecimal.class),
                any(Boolean.class)
        )).thenReturn(mockProduct);

        mockMvc.perform(put("/api/v1/trackings/{productId}", productId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.targetPrice").value(100));
    }

    @Test
    void shouldReturnForbidden_WhenUserIsNotOwner() throws Exception {

        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doThrow(new AccessDeniedException("You do not have permission to delete this tracked product."))
                .when(trackingUseCase).deleteTrackedProduct(productId, userId);

        mockMvc.perform(delete("/api/v1/trackings/{productId}", productId)
                        .param("userId", userId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorReason").value("ACCESS_DENIED"));
    }

    @Test
    void shouldReturnNoContent_WhenDeletingProduct() throws Exception {

        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing().when(trackingUseCase).deleteTrackedProduct(userId, productId);

        mockMvc.perform(delete("/api/v1/trackings/{productId}", productId)
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnHistory_WhenProductExists() throws Exception {

        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        List<PriceHistory> historyList = List.of(
                new PriceHistory(UUID.randomUUID(), productId, BigDecimal.valueOf(100), Instant.now().minusSeconds(3600)),
                new PriceHistory(UUID.randomUUID(), productId, BigDecimal.valueOf(110), Instant.now())
        );

        when(trackingUseCase.getPriceHistory(eq(productId), eq(userId), any(Pageable.class)))
                .thenReturn(historyList);

        mockMvc.perform(get("/api/v1/trackings/{productId}/history", productId)
                        .param("userId", userId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].price").value(100))
                .andExpect(jsonPath("$[1].price").value(110));
    }

    @Test
    void shouldReturnScanCandidates_WhenRequested() throws Exception {

        TrackedProduct mockProduct = TrackedProduct.builder()
                .id(UUID.randomUUID())
                .url("https://test.com")
                .productName("Test Product")
                .targetPrice(BigDecimal.valueOf(100))
                .isActive(true)
                .build();

        when(trackingUseCase.getProductsToScan(any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(mockProduct));

        mockMvc.perform(get("/api/v1/trackings/scan-candidates")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("Test Product"));
    }

    @Test
    void shouldReturnOk_When_ProductExistsButInactive_Reactivate() throws Exception {

        UUID userId = UUID.randomUUID();

        CreateTrackingRequest request = new CreateTrackingRequest(
                "https://www.trendyol.com/laptop-p-123",
                new BigDecimal("14000.00")
        );

        TrackedProduct reactivatedProduct = TrackedProduct.builder()
                .id(UUID.randomUUID())
                .url(request.url())
                .targetPrice(request.targetPrice())
                .isActive(true)
                .build();

        when(trackingUseCase.createTrackedProduct(any(UUID.class), eq(request.url()), eq(request.targetPrice())))
                .thenReturn(reactivatedProduct);

        mockMvc.perform(post("/api/v1/trackings")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(request.url()))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.targetPrice").value(14000.00));
    }

    @Test
    void shouldReturnConflict_When_ProductIsAlreadyActive() throws Exception {

        UUID userId = UUID.randomUUID();

        CreateTrackingRequest request = new CreateTrackingRequest(
                "https://www.trendyol.com/laptop-p-123",
                new BigDecimal("15000.00")
        );

        when(trackingUseCase.createTrackedProduct(any(UUID.class), eq(request.url()), eq(request.targetPrice())))
                .thenThrow(new ResourceAlreadyExistsException("Tracked Product", "url", request.url()));

        mockMvc.perform(post("/api/v1/trackings")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorReason").value("RESOURCE_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }


}
