package com.challenge.meli.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryAvailabilityResponse(
        @NotNull String productId,
        @NotNull List<StoreStockDto> availability,
        @NotNull MetadataDto metadata
) {

    public record StoreStockDto(
            @NotNull String storeId,
            @NotNull String storeName,
            @NotNull LocationDto location,
            @NotNull StockInfoDto stock,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            @NotNull LocalDateTime lastUpdate,
            @NotNull String confidence,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime estimatedRefreshTime
    ) {}

    public record LocationDto(
            @NotNull Double latitude,
            @NotNull Double longitude,
            @NotNull String address
    ) {}

    public record StockInfoDto(
            @NotNull Integer available,
            @NotNull Integer reserved,
            @NotNull Integer total
    ) {}

    public record MetadataDto(
            @NotNull String requestId,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            @NotNull LocalDateTime timestamp,
            @NotNull Boolean cacheHit
    ) {}
}
