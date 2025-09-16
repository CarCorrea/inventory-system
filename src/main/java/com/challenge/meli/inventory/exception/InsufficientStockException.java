package com.challenge.meli.inventory.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends InventoryServiceException {
    private final String productId;
    private final String storeId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    public InsufficientStockException(String productId, String storeId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                productId, storeId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.storeId = storeId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
}
