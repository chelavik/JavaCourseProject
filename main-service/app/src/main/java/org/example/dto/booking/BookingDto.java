package org.example.dto.booking;

import lombok.*;
import org.example.dto.user.UserDto;
import org.example.dto.workspace.WorkspaceDto;

import java.time.LocalDateTime;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;
    private WorkspaceDto workspace;
    private UserDto user;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean cancelled;
}
