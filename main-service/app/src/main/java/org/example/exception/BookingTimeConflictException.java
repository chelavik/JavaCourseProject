package org.example.exception;

public class BookingTimeConflictException extends ApiException {
    public BookingTimeConflictException(Long id) {
        super("Booking with id " + id + " is busy.");
    }
}