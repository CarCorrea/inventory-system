package com.challenge.meli.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ReservationResponse(
        @NotNull String reservationId,
        @NotNull String status,
        @NotNull String productId,
        @NotNull String storeId,
        @NotNull Integer quantity,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull LocalDateTime expiresAt,
        @NotNull String confirmationCode,
        @NotNull Map<String, String> actions
) {
}
