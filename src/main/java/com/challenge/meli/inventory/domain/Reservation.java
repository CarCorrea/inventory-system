package com.challenge.meli.inventory.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    private String reservationId;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private String productId;

    @NotNull
    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "customer_id")
    private String customerId;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    public Reservation(String reservationId, String productId, String storeId,
                       String customerId, Integer quantity, LocalDateTime expiresAt) {
        this.reservationId = reservationId;
        this.productId = productId;
        this.storeId = storeId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.status = ReservationStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.confirmationCode = generateConfirmationCode();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isActive() {
        return this.status == ReservationStatus.ACTIVE && !isExpired();
    }

    public void confirm() {
        if (!isActive()) {
            throw new IllegalStateException("Cannot confirm inactive or expired reservation");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel confirmed reservation");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public void expire() {
        if (this.status == ReservationStatus.ACTIVE) {
            this.status = ReservationStatus.EXPIRED;
        }
    }

    private String generateConfirmationCode() {
        return "CONF-" + System.currentTimeMillis() % 10000;
    }

    public enum ReservationStatus {
        ACTIVE, CONFIRMED, CANCELLED, EXPIRED
    }
}
