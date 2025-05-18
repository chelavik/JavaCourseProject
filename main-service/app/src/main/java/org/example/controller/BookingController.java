package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.dto.booking.UpdateBookingRequest;
import org.example.service.TokenValidationService;
import org.example.service.booking.BookingService;
import org.example.validator.BookingTimeValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class BookingController {

    private final BookingService bookingService;
    private final TokenValidationService tokenValidationService;
    private final BookingTimeValidator bookingTimeValidator;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(
            @Valid @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateBookingRequest request) {

        bookingTimeValidator.validateBooking(request.getStartTime(), request.getEndTime());

        UUID userId = tokenValidationService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        BookingDto booking = bookingService.createBooking(request, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getMyBookings(
            @Valid @RequestHeader("Authorization") String token) {

        UUID userId = tokenValidationService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<BookingDto> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @Valid @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        UUID userId = tokenValidationService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        bookingService.cancelBooking(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateOwnBooking(@RequestHeader("Authorization") String token,
                                                @Valid @PathVariable Long id,
                                                @Valid @RequestBody UpdateBookingRequest request) {
        UUID userId = tokenValidationService.getUserIdFromToken(token);
        bookingService.updateUserBookingTime(id, userId, request.getStartTime(), request.getEndTime());
        return ResponseEntity.noContent().build();
    }

}
