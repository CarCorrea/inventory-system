package com.challenge.meli.inventory.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;


import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Double price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Product() {}

    public Product(String productId, String name, String description, Double price) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /*@PrePersist
    public void ensureId() {
        if (this.productId == null || this.productId.isBlank()) {
            this.productId = UUID.randomUUID().toString(); // fallback automático
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }*/

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
