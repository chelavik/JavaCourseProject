package org.example.validator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.example.exception.BookingTimeValidationException;
import org.springframework.stereotype.Component;

@Component
public class BookingTimeValidator {

    private static final LocalTime START_TIME = LocalTime.of(10, 0);
    private static final LocalTime END_TIME = LocalTime.of(22, 0);
    private static final Duration SLOT_DURATION = Duration.ofMinutes(30);
    private static final int MAX_SLOTS = 10;

    public void validateBooking(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new BookingTimeValidationException("End time must be after start time.");
        }

        // Проверка кратности 30 минут
        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes % SLOT_DURATION.toMinutes() != 0) {
            throw new BookingTimeValidationException("Booking duration must be multiple of 30 minutes.");
        }

        // Проверка длительности
        if (durationMinutes > SLOT_DURATION.toMinutes() * MAX_SLOTS) {
            throw new BookingTimeValidationException("Booking cannot exceed 5 hours.");
        }

        // Проверка начала и конца по времени дня
        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();

        if (start.isBefore(START_TIME) || end.isAfter(END_TIME)) {
            throw new BookingTimeValidationException("Booking must be between 10:00 and 22:00.");
        }
    }
}
