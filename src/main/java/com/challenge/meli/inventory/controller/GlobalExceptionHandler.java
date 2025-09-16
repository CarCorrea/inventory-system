package com.challenge.meli.inventory.controller;

import com.challenge.meli.inventory.exception.InsufficientStockException;
import com.challenge.meli.inventory.exception.InventoryServiceException;
import com.challenge.meli.inventory.exception.ReservationExpiredException;
import com.challenge.meli.inventory.exception.ReservationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientStock(InsufficientStockException exception, WebRequest request){
        logger.warn("Insufficient stock: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("InsufficientStock");
        problem.setProperty("productId", exception.getProductId());
        problem.setProperty("storeId", exception.getStoreId());
        problem.setProperty("requestedQuantity", exception.getRequestedQuantity());
        problem.setProperty("availableQuantity", exception.getAvailableQuantity());
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleReservationNotFound(ReservationNotFoundException exception, WebRequest request){
        logger.warn("Reservation not found: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Reservation Not Found");
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ReservationExpiredException.class)
    public ResponseEntity<ProblemDetail> handleReservationExpired(ReservationExpiredException exception, WebRequest request){
        logger.warn("Reservation expired: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, exception.getMessage());
        problem.setTitle("Reservation Expired");
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.GONE).body(problem);
    }

    @ExceptionHandler(InventoryServiceException.class)
    public ResponseEntity<ProblemDetail> handleInventoryService(InventoryServiceException exception, WebRequest request){
        logger.warn("Inventory service error: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Inventory service error: {}");
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLocking(OptimisticLockingFailureException exception, WebRequest request){
        logger.warn("Optimistic locking failure: {}", exception.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Resource was modified by another transaction. Please retry.");
        problem.setTitle("Concurrent Modification");
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Validation Error");
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception exception, WebRequest request) {
        logger.error("Unexpected error: {}", exception.getMessage(), exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
