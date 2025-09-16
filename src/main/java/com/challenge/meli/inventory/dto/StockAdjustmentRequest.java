package com.challenge.meli.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record StockAdjustmentRequest(
        @NotEmpty @Valid List<AdjustmentDto> adjustments,
        @NotBlank String batchId,
        @NotBlank String source
) {

    public record AdjustmentDto(
            @NotBlank String productId,
            @NotBlank String storeId,
            @NotNull Integer delta,
            @NotBlank String reason,
            String referenceId,
            LocalDateTime timestamp
    ) {}
}
