package org.example.dto.user;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateUserRequest {
    private UUID id;
    private String email;
}
