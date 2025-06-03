package org.example.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class UpdateUserRequest {
    @Size(min = 3, max = 50, message = "Name must be at between 3 and 50 characters")
    private String name;
}
