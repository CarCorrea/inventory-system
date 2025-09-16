package com.challenge.meli.inventory.repository;

import com.challenge.meli.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {


    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
}
