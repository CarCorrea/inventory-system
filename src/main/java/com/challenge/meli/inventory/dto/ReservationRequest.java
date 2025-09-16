package com.challenge.meli.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public record ReservationRequest(
        @NotBlank String productId,
        @NotBlank String storeId,
        @NotNull @Positive Integer quantity,
        String customerId,
        Integer reservationTtl,
        Map<String, Object> metadata) {
}
