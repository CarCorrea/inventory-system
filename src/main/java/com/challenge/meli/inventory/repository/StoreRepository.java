package com.challenge.meli.inventory.repository;

import com.challenge.meli.inventory.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {

    List<Store> findByNameContainingIgnoreCase(String name);

    @Query("SELECT s FROM Store s WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) * " +
            "cos(radians(s.longitude) - radians(:lon)) + sin(radians(:lat)) * " +
            "sin(radians(s.latitude)))) < :radius")
    List<Store> findStoresWithinRadius(@Param("lat") Double latitude,
                                       @Param("lon") Double longitude,
                                       @Param("radius") Double radius);
}
