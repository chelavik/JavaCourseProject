package org.example.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;

    @Pattern(regexp = "ADMIN|USER", message = "Role must be either ADMIN or USER")
    private String role;
}

