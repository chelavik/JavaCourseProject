package org.example.runner;

import java.time.LocalDateTime;
import java.util.List;

import org.example.entity.Booking;
import org.example.repository.BookingRepository;
import org.example.service.booking.BookingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingAutoCloser {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Scheduled(fixedDelay = 60000) // запускается каждые 60 секунд
    public void closeExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findAllByEndTimeBeforeAndCancelledFalse(now);
        for (Booking booking : expiredBookings) {
            bookingService.cancelBooking(booking.getId(), booking.getUser().getId());
        }
    }
}
