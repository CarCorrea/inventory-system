package com.challenge.meli.inventory.exception;

public class ReservationExpiredException extends RuntimeException {
    public ReservationExpiredException(String reservationId) {
        super("Reservation has expired: " + reservationId);
    }
}
