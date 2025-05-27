package org.example.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.example.dto.booking.BookingDto;
import org.example.dto.booking.UpdateBookingRequest;
import org.example.dto.user.AdminUpdateUserRequest;
import org.example.dto.user.UserDto;
import org.example.dto.workspace.CreateWorkspaceRequest;
import org.example.dto.workspace.WorkspaceDto;
import org.example.service.TokenValidationService;
import org.example.service.booking.BookingService;
import org.example.service.user.UserService;
import org.example.service.workspace.WorkspaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class AdminController {

    private final BookingService bookingService;
    private final UserService userService;
    private final WorkspaceService workspaceService;
    private final TokenValidationService tokenValidationService;
    
    // bookings

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookings(@Valid @RequestHeader("Authorization") String token) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingDto> getBookingById(@Valid @RequestHeader("Authorization") String token,
                                                    @PathVariable Long id) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> cancelBooking(@Valid @RequestHeader("Authorization") String token,
                                            @PathVariable Long id) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bookingService.cancelBooking(id, bookingService.getBookingById(id).getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/bookings/{id}")
    public ResponseEntity<Void> updateBookingAsAdmin(@Valid @RequestHeader("Authorization") String token,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody UpdateBookingRequest request) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        bookingService.updateAdminBookingTime(id, request.getStartTime(), request.getEndTime());
        return ResponseEntity.noContent().build();
    }

    // users

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(@Valid @RequestHeader("Authorization") String token) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@Valid @RequestHeader("Authorization") String token,
                                           @PathVariable UUID id) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<Void> updateUser(@Valid @RequestHeader("Authorization") String token,
                                        @PathVariable UUID id,
                                        @Valid @RequestBody AdminUpdateUserRequest request) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.adminUpdateUser(id, request.getName(), request.getRole());
        return ResponseEntity.noContent().build();
    }

    // workspaces
    @GetMapping("/workspaces")
    public ResponseEntity<List<WorkspaceDto>> getAllWorkspaces(@Valid @RequestHeader("Authorization") String token) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(workspaceService.findAll());   
    }

    @PostMapping("/workspaces")
    public ResponseEntity<WorkspaceDto> createWorkspace(@Valid @RequestHeader("Authorization") String token,
                                                        @Valid @RequestBody CreateWorkspaceRequest request) {
        if (!tokenValidationService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return new ResponseEntity<>(workspaceService.createWorkspace(request), HttpStatus.CREATED);
    }

    @PatchMapping("/workspaces/{id}")
    public ResponseEntity<WorkspaceDto> updateWorkspaceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        boolean active = (boolean) updates.get("active");
        WorkspaceDto updated = workspaceService.updateStatus(id, active);
        return ResponseEntity.ok(updated);
    }

}
