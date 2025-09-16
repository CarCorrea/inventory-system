package com.challenge.meli.inventory.controller;

import com.challenge.meli.inventory.dto.*;
import com.challenge.meli.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/inventory")
@Tag(name = "Inventorry Management", description = "APIs for inventory and stock managent")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}/availability")
    @Operation(summary = "Get product availability",
                description = "Retrieve availability information for a product across stores")
    @ApiResponse(responseCode = "200", description = "Availability information retrieved successfully")
    public ResponseEntity<InventoryAvailabilityResponse> getAvailability(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Store ID (optional)") @RequestParam(required = false) String storeId){

        logger.info("Getting availability for product {} in store {}", productId, storeId);

        InventoryAvailabilityResponse response = inventoryService.getAvailability(productId, storeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve product",
                description = "Create a temporary reservation for a product")
    @ApiResponse(responseCode = "201", description = "Reservation created successfully")
    @ApiResponse(responseCode = "409", description = "Insufficient stock")
    public ResponseEntity<ReservationResponse> reserveProduct(@Valid @RequestBody ReservationRequest request){
        logger.info("Creating reservation for product {} in store",
                request.productId(), request.storeId());

        ReservationResponse response = inventoryService.reserveProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/release/{reservationId}")
    @Operation(summary = "release reservation",
                description = "Release a product reservation")
    @ApiResponse(responseCode = "204", description = "Reservation released successfully")
    public ResponseEntity<Void> releaseReservation(@PathVariable String reservationId) {
        logger.info("Releasing reservation: {}", reservationId);

        inventoryService.releaseReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm/{reservationId}")
    @Operation(summary = "Confirm reserrvation",
                description = "Confirm a product reservation")
    @ApiResponse(responseCode = "204", description = "Reservation confirmed successfully")
    public ResponseEntity<Void> confirmReserrvation(@PathVariable String reservationId) {
        logger.info("Confirming reservation: {}", reservationId);

        inventoryService.confirmReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/adjust")
    @Operation(summary = "Adjust stock",
                description = "Batch adjust stock levels (internal use)")
    @ApiResponse(responseCode = "200", description = "Stock adjustment processed")
    public ResponseEntity<StockAdjustmentResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request){
        logger.info("Processing stock adjustment batch: {}",
                request.batchId());

        StockAdjustmentResponse response = inventoryService.adjustStock(request);
        return ResponseEntity.ok(response);
    }
}
