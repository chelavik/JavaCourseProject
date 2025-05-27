package org.example.service.booking;

import org.example.dto.booking.BookingDto;
import org.example.dto.booking.CreateBookingRequest;
import org.example.entity.Booking;
import org.example.entity.User;
import org.example.entity.Workspace;
import org.example.exception.*;
import org.example.repository.BookingRepository;
import org.example.repository.UserRepository;
import org.example.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    private BookingRepository bookingRepository;
    private UserRepository userRepository;
    private WorkspaceRepository workspaceRepository;
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        userRepository = mock(UserRepository.class);
        workspaceRepository = mock(WorkspaceRepository.class);
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, workspaceRepository);
    }


    @Test
    void testCreateBooking_WorkspaceNotFound() {
        UUID userId = UUID.randomUUID();
        CreateBookingRequest request = CreateBookingRequest.builder()
                .workspaceId(123L)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        when(workspaceRepository.findById(request.getWorkspaceId())).thenReturn(Optional.empty());

        assertThrows(WorkspaceNotFoundException.class, () -> bookingService.createBooking(request, userId));
    }

    @Test
    void testCreateBooking_WorkspaceNotAvailable() {
        UUID userId = UUID.randomUUID();
        Long workspaceId = 1L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .active(true)
                .build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .workspaceId(workspaceId)
                .startTime(start)
                .endTime(end)
                .build();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(bookingRepository.existsByWorkspaceAndCancelledFalseAndStartTimeLessThanAndEndTimeGreaterThan(workspace, end, start)).thenReturn(true);

        assertThrows(WorkspaceNotAvailableException.class, () -> bookingService.createBooking(request, userId));
    }

    @Test
    void testCreateBooking_UserNotFound() {
        UUID userId = UUID.randomUUID();
        Long workspaceId = 1L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        Workspace workspace = Workspace.builder()
                .id(workspaceId)
                .active(true)
                .build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .workspaceId(workspaceId)
                .startTime(start)
                .endTime(end)
                .build();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(bookingRepository.existsByWorkspaceAndCancelledFalseAndStartTimeLessThanAndEndTimeGreaterThan(workspace, end, start)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> bookingService.createBooking(request, userId));
    }

    @Test
    void testCancelBooking_Success() {
        UUID userId = UUID.randomUUID();
        Long bookingId = 10L;

        User user = User.builder()
                .id(userId)
                .build();

        Workspace workspace = Workspace.builder()
                .id(1L)
                .build();

        Booking booking = Booking.builder()
                .id(bookingId)
                .user(user)
                .workspace(workspace)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .cancelled(false)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        bookingService.cancelBooking(bookingId, userId);

        assertTrue(booking.isCancelled());
    }

    @Test
    void testCancelBooking_BookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.cancelBooking(999L, UUID.randomUUID()));
    }

    @Test
    void testCancelBooking_NoRights() {
        UUID userId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        Long bookingId = 1L;

        User otherUser = User.builder().id(anotherUserId).build();

        Booking booking = Booking.builder()
                .id(bookingId)
                .user(otherUser)
                .cancelled(false)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(NoRightsException.class, () -> bookingService.cancelBooking(bookingId, userId));
    }
}
