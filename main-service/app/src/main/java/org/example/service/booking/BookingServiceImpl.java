package org.example.service.booking;

import lombok.RequiredArgsConstructor;
import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.dto.user.UserDto;
import org.example.dto.workspace.WorkspaceDto;
import org.example.entity.Booking;
import org.example.entity.User;
import org.example.entity.Workspace;
import org.example.exception.BookingNotFoundException;
import org.example.exception.BookingTimeConflictException;
import org.example.exception.NoRightsException;
import org.example.exception.UserNotFoundException;
import org.example.exception.WorkspaceNotAvailableException;
import org.example.exception.WorkspaceNotFoundException;
import org.example.repository.BookingRepository;
import org.example.repository.UserRepository;
import org.example.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    public BookingDto createBooking(CreateBookingRequest request, UUID userId) {
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException(request.getWorkspaceId()));

        boolean isBusy = bookingRepository.existsByWorkspaceAndCancelledFalseAndStartTimeLessThanAndEndTimeGreaterThan(
                workspace, request.getEndTime(), request.getStartTime()
        );

        if (isBusy) {
            throw new WorkspaceNotAvailableException(request.getWorkspaceId());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Booking booking = Booking.builder()
                .user(user)
                .workspace(workspace)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .cancelled(false)
                .build();

        return mapToDto(bookingRepository.save(booking));
    }

    @Override
    public List<BookingDto> getUserBookings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return bookingRepository.findByUserAndCancelledFalse(user).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void cancelBooking(Long bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (!booking.getUser().getId().equals(userId)) {
            throw new NoRightsException(userId);
        }
        booking.setCancelled(true);
        bookingRepository.save(booking);
    }

    @Override
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public BookingDto getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .map(this::mapToDto)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    @Override
    public void updateUserBookingTime(Long bookingId, UUID userId, LocalDateTime start, LocalDateTime end) {
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (!booking.getUser().getId().equals(userId)) {
                throw new NoRightsException(userId);
        }
        updateTimeIfAvailable(booking, start, end);
    }

    @Override
    public void updateAdminBookingTime(Long bookingId, LocalDateTime start, LocalDateTime end) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        updateTimeIfAvailable(booking, start, end);
    }

    private void updateTimeIfAvailable(Booking booking, LocalDateTime start, LocalDateTime end) {
        boolean isBusy = bookingRepository.existsByWorkspaceAndCancelledFalseAndStartTimeLessThanAndEndTimeGreaterThan(
                booking.getWorkspace(), end, start
        );
        if (isBusy) {
                throw new BookingTimeConflictException(booking.getWorkspace().getId());
        }
        booking.setStartTime(start);
        booking.setEndTime(end);
        bookingRepository.save(booking);
    }

    private BookingDto mapToDto(Booking b) {
        UserDto userDto = UserDto.builder()
                .id(b.getUser().getId())
                .email(b.getUser().getEmail())
                .name(b.getUser().getName())
                .role(b.getUser().getRole().name())
                .build();

        WorkspaceDto workspaceDto = WorkspaceDto.builder()
                .id(b.getWorkspace().getId())
                .name(b.getWorkspace().getName())
                .capacity(b.getWorkspace().getCapacity())
                .active(b.getWorkspace().isActive())
                .build();

        return BookingDto.builder()
                .id(b.getId())
                .user(userDto)
                .workspace(workspaceDto)
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .cancelled(b.isCancelled())
                .build();
    }
}
