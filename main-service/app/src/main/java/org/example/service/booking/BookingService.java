package org.example.service.booking;

import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingDto createBooking(CreateBookingRequest request, UUID userId);
    List<BookingDto> getUserBookings(UUID userId);
    void cancelBooking(Long bookingId, UUID userId);
    BookingDto getBookingById(Long id);
    void updateUserBookingTime(Long bookingId, UUID userId, LocalDateTime start, LocalDateTime end);
    

    // методы для админских ручек
    List<BookingDto> getAllBookings();
    void updateAdminBookingTime(Long bookingId, LocalDateTime start, LocalDateTime end);
}
