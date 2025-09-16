package com.challenge.meli.inventory.exception;

public class ReservationNotFoundException extends InventoryServiceException  {
    public ReservationNotFoundException(String reservationId) {
        super("Reservation has expired: " + reservationId);
    }
}
