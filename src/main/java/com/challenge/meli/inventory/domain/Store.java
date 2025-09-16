package com.challenge.meli.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stores")
public class Store {

    @Id
    private String storeId;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Store() {}

    public Store(String storeId, String name, String address, Double latitude, Double longitude) {
        this.storeId = storeId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = LocalDateTime.now();
    }
}
