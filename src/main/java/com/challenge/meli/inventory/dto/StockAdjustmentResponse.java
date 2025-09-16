package com.challenge.meli.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record StockAdjustmentResponse(
        @NotNull String batchId,
        @NotNull List<AdjustmentResultDto> results,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull LocalDateTime processedAt
) {

    public record AdjustmentResultDto(
            @NotNull String productId,
            @NotNull String storeId,
            @NotNull String status,
            Integer previousStock,
            Integer newStock,
            String eventId,
            String errorMessage
    ) {}
}
