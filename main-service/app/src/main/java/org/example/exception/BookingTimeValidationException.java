package org.example.exception;


public class BookingTimeValidationException extends ApiException {
    public BookingTimeValidationException(String message) {
        super(message);
    }
}
