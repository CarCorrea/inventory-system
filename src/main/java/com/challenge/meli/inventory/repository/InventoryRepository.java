package com.challenge.meli.inventory.repository;

import com.challenge.meli.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.storeId = :storeId")
    Optional<Inventory> findByProductIdAndStoreIdWithLock(@Param("productId") String productId,@Param("storeId") String storeId);

    Optional<Inventory> findByProductIdAndStoreId(String productId, String storeId);

    List<Inventory> findByStoreId(String storeId);

    List<Inventory> findByProductId(String productId);

    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.available > 0")
    List<Inventory> findAvailableByProductId(@Param("productId") String productId);

    @Query("SELECT i FROM Inventory i WHERE i.storeId = :storeId AND i.available < :threshold")
    List<Inventory> findLowStockByStore(@Param("storeId") String storeId,
                                        @Param("threshold") Integer threshold);
}
