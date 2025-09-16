package com.challenge.meli.inventory.repository;

import com.challenge.meli.inventory.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {

    List<Reservation> findByCustomerId(String customerId);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
    List<Reservation> findExpiredActiveReservations(@Param("now")LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.productId = :productId AND r.storeId = :storeId AND r.status = 'ACTIVE'")
    List<Reservation> findActiveReservationsByProductAndStore(@Param("productId") String productId,
                                                            @Param("storeId") String storeId);
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.createdAt BETWEEN :start AND :end")
    List<Reservation> findByStatusAndDataRange(@Param("status") Reservation.ReservationStatus status,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}
