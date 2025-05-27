package org.example.dto.booking;

import lombok.*;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class CreateBookingRequest {
    @NotNull(message = "Workspace id must pe provided")
    private Long workspaceId;

    @NotNull(message = "Start time must be provided")
    @FutureOrPresent(message = "Start time must be in the present or future")
    private LocalDateTime startTime;

    @NotNull(message = "End time must be provided")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
}
