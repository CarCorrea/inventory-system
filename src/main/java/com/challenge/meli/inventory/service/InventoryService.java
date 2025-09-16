package com.challenge.meli.inventory.service;

import com.challenge.meli.inventory.domain.Inventory;
import com.challenge.meli.inventory.domain.Product;
import com.challenge.meli.inventory.domain.Reservation;
import com.challenge.meli.inventory.domain.Store;
import com.challenge.meli.inventory.dto.*;
import com.challenge.meli.inventory.exception.InsufficientStockException;
import com.challenge.meli.inventory.exception.InventoryServiceException;
import com.challenge.meli.inventory.exception.ReservationExpiredException;
import com.challenge.meli.inventory.exception.ReservationNotFoundException;
import com.challenge.meli.inventory.repository.InventoryRepository;
import com.challenge.meli.inventory.repository.ProductRepository;
import com.challenge.meli.inventory.repository.ReservationRepository;
import com.challenge.meli.inventory.repository.StoreRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    @Value("${inventory.reservation.default-ttl:1800}")
    private Integer defaultReservationTtl;

    private final Map<String, InventoryAvailabilityResponse> availabilityCache = new ConcurrentHashMap<>();

    public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository, StoreRepository storeRepository, ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.reservationRepository = reservationRepository;
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "getAvailabilityFallback")
    @Cacheable(value = "inventory-availability", key = "#productId + ':' + (#storeId ?: 'all')")
    @Transactional(readOnly = true)
    public InventoryAvailabilityResponse getAvailability(String productId, String storeId) {
        logger.debug("Getting availability for product {} in store {}", productId, storeId);

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new InventoryServiceException("Product not found: " + productId));

        List<Inventory> inventories;

        if (storeId != null){
            inventories = inventoryRepository.findByProductIdAndStoreId(productId, storeId)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            inventories = inventoryRepository.findByProductId(productId);
        }

        List<InventoryAvailabilityResponse.StoreStockDto> availability = inventories.stream()
                .map(this::mapToStoreStockDto)
                .collect(Collectors.toList());

        String requestId = UUID.randomUUID().toString();
        InventoryAvailabilityResponse.MetadataDto metadata =
        new InventoryAvailabilityResponse.MetadataDto(requestId, LocalDateTime.now(), false);

        return new InventoryAvailabilityResponse(productId, availability, metadata);
    }

    @Retryable(value = {OptimisticLockingFailureException.class},
                maxAttempts = 3,
                backoff = @Backoff(delay = 100))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ReservationResponse reserveProduct(ReservationRequest request){
        logger.info("Creating rreservation for product {} in store {} with quantity {}",
                request.productId(), request.storeId(), request.quantity());

        Inventory inventory = inventoryRepository.findByProductIdAndStoreIdWithLock(request.productId(), request.storeId())
                .orElseThrow(() -> new InventoryServiceException("No inventory found for product " + request.productId() + " in store " + request.storeId()));

        if (!inventory.canReserve(request.quantity())){
            throw new InsufficientStockException(
                    request.productId(), request.storeId(), request.quantity(), inventory.getAvailable()
            );
        }

        String reservationId = "RES-" +  UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(request.reservationTtl() != null ? request.reservationTtl() : defaultReservationTtl);

        Reservation reservation = new Reservation(
                reservationId, request.productId(), request.storeId(), request.customerId(), request.quantity(), expiresAt
        );

        inventory.reserve(request.quantity());
        inventoryRepository.save(inventory);
        reservationRepository.save(reservation);

        invalidateAvailabilityCache(request.productId());

        logger.info("Reservation created successfully: {}", reservationId);

        Map<String, String> actions = Map.of(
            "confirm", "/api/v1/inventory/confirm/" + reservationId,
            "release", "/api/v1/inventory/release/" + reservationId,
            "extend", "/api/v1/inventory/extend" + reservationId
        );

        return new ReservationResponse(reservationId, "CONFIRMED",
                request.productId(),
                request.storeId(), request.quantity(),
                expiresAt, reservation.getConfirmationCode(),
                actions);

    }

    @Retryable(value = {OptimisticLockingFailureException.class},
                maxAttempts = 3,
                backoff = @Backoff(delay = 100))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void releaseReservation(String reservationId){
        logger.info("Releasingn rreserrvation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!reservation.isActive()){
            throw new InventoryServiceException("Reservation is not active: " + reservationId);
        }

        Inventory inventory = inventoryRepository.findByProductIdAndStoreIdWithLock(reservation.getProductId(),
                reservation.getStoreId())
                .orElseThrow(() -> new InventoryServiceException("Inventory not found"));

        inventory.releaseReservation(reservation.getQuantity());
        reservation.cancel();

        invalidateAvailabilityCache(reservation.getProductId());

        logger.info("Reservation released successfully");
    }

    @Retryable(value = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void confirmReservation(String reservationId){
        logger.info("Confirming reservation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.isExpired()) {
            throw new ReservationExpiredException(reservationId);
        }
        
        if (!reservation.isActive()){
            throw new InventoryServiceException("Reservation is not active: " + reservationId);
        }

        Inventory inventory = inventoryRepository
                .findByProductIdAndStoreIdWithLock(reservation.getProductId(), reservation.getStoreId())
                .orElseThrow(() -> new InventoryServiceException("Inventory not found: "));

        inventory.confirmSale(reservation.getQuantity());
        reservation.confirm();

        inventoryRepository.save(inventory);
        reservationRepository.save(reservation);

        invalidateAvailabilityCache(reservation.getProductId());

        logger.info("Reservation conformed successfully: {}", reservationId);
    }

    public StockAdjustmentResponse adjustStock(StockAdjustmentRequest request){
        logger.info("Processing stock adjustment batch {}", request.batchId());

        List<StockAdjustmentResponse.AdjustmentResultDto> results = request.adjustments().stream()
                .map(this::processStockAdjustment)
                .collect(Collectors.toList());

        return new StockAdjustmentResponse(request.batchId(), results, LocalDateTime.now());
    }

    //concon@niusushi.cl

    private StockAdjustmentResponse.AdjustmentResultDto processStockAdjustment(StockAdjustmentRequest.AdjustmentDto adjustment){
        try {
            Inventory inventory = inventoryRepository.findByProductIdAndStoreIdWithLock(adjustment.productId(), adjustment.storeId())
                    .orElseThrow(() -> new InventoryServiceException("Inventory not found"));

            Integer previousStock = inventory.getTotal();
            inventory.adjustStock(adjustment.delta());
            inventoryRepository.save(inventory);

            invalidateAvailabilityCache(adjustment.productId());

            String eventId = "EVT-" + UUID.randomUUID().toString();

            return new StockAdjustmentResponse.AdjustmentResultDto(
                    adjustment.productId(), adjustment.storeId(), "SUCCESS",
                    previousStock, inventory.getTotal(), eventId, null
            );

        } catch (Exception e) {
            logger.error("Error processing stock adjustment: {}", e.getMessage());
            return new StockAdjustmentResponse.AdjustmentResultDto(
                    adjustment.productId(), adjustment.storeId(), "ERROR",
                    null, null, null, e.getMessage()
            );
        }
    }
    @Transactional
    public void cleanupExpiredReservation() {
        logger.debug("cleaning up expired reservations");

        List<Reservation> expiredReservation = reservationRepository
                .findExpiredActiveReservations(LocalDateTime.now());

        for(Reservation reservation : expiredReservation) {
            try {
                releaseExpiredReservation(reservation);
            }catch (Exception e){
                logger.error("Error releasing expired reservation {}: {}",
                        reservation.getReservationId(), e.getMessage());
            }
        }
        logger.info("Cleaned up {} expired reservations", expiredReservation.size());
    }

    private void releaseExpiredReservation(Reservation reservation) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndStoreIdWithLock(reservation.getProductId(), reservation.getStoreId())
                .orElseThrow(() -> new InventoryServiceException("Inventory not found"));

        inventory.releaseReservation(reservation.getQuantity());
        reservation.expire();

        inventoryRepository.save(inventory);
        reservationRepository.save(reservation);

        invalidateAvailabilityCache(reservation.getProductId());
    }

    private InventoryAvailabilityResponse.StoreStockDto mapToStoreStockDto(Inventory inventory){
        Store store = storeRepository.findById(inventory.getStoreId())
                .orElseThrow(() -> new InventoryServiceException("Store not found: " + inventory.getStoreId()));

        InventoryAvailabilityResponse.LocationDto location = new InventoryAvailabilityResponse.LocationDto(store.getLatitude(), store.getLongitude(), store.getAddress());

        InventoryAvailabilityResponse.StockInfoDto stock =
                new InventoryAvailabilityResponse.StockInfoDto(inventory.getAvailable(), inventory.getReserved(), inventory.getTotal());


        return new InventoryAvailabilityResponse.StoreStockDto(store.getStoreId(), store.getName(), location, stock,
                inventory.getLastUpdated(), "HIGH", inventory.getLastUpdated().plusMinutes(5));
    }

    private void invalidateAvailabilityCache(String productId) {
        availabilityCache.entrySet().removeIf(entry -> entry.getKey().startsWith(productId + ":"));
        logger.debug("Invalidated cache for product: {}", productId);
    }

    public InventoryAvailabilityResponse getAvailabilityFallback(String productId, String storeId, Exception ex) {
        logger.warn("Circuit breaker activated for availability query. Using fallback.", ex);

        String cacheKey = productId + ":" + (storeId != null ? storeId : "all");
        InventoryAvailabilityResponse cached = availabilityCache.get(cacheKey);

        if (cached != null) {
            return cached;
        }

        String requestId = UUID.randomUUID().toString();
        InventoryAvailabilityResponse.MetadataDto metadata =
                new InventoryAvailabilityResponse.MetadataDto(requestId, LocalDateTime.now(), true);

        return new InventoryAvailabilityResponse(productId, List.of(), metadata);
    }
}
