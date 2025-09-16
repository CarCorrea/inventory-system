package com.challenge.meli.inventory.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private String productId;

    @NotNull
    @Column(name = "store_id", nullable = false)
    private String storeId;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer available;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer reserved;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer total;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Version
    private Long version;

    public Inventory() {}

    public Inventory(String productId, String storeId, Integer total) {
        this.productId = productId;
        this.storeId = storeId;
        this.total = total;
        this.available = total;
        this.reserved = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    /*@PreUpdate
    @PrePersist
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
        this.total = this.available + this.reserved;
    }*/

    public boolean canReserve(Integer quantity) {
        return this.available >= quantity;
    }

    public void reserve(Integer quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalArgumentException("Insufficient stock available");
        }
        this.available -= quantity;
        this.reserved += quantity;
    }

    public void releaseReservation(Integer quantity) {
        if (this.reserved < quantity) {
            throw new IllegalArgumentException("Cannot release more than reserved");
        }
        this.reserved -= quantity;
        this.available += quantity;
    }

    public void confirmSale(Integer quantity) {
        if (this.reserved < quantity) {
            throw new IllegalArgumentException("Cannot confirm more than reserved");
        }
        this.reserved -= quantity;
        this.total -= quantity;
    }

    public void adjustStock(Integer delta) {
        int newAvailable = this.available + delta;
        if (newAvailable < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.available = newAvailable;
        this.total = this.available + this.reserved;
    }
}
