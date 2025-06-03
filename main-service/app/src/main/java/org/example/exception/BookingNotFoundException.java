package org.example.exception;

public class BookingNotFoundException extends ApiException {
    public BookingNotFoundException(Long id) {
        super("Booking with id " + id + " not found.");
    }
}