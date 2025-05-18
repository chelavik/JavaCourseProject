package org.example.controller;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.example.dto.user.UpdateUserRequest;
import org.example.dto.user.UserDto;
import org.example.service.TokenValidationService;
import org.example.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class UserController {

    private final UserService userService;
    private final TokenValidationService tokenValidationService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@Valid @RequestHeader("Authorization") String token) {
        UUID userId = tokenValidationService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            @Valid @RequestHeader("Authorization") String token,
            @RequestBody UpdateUserRequest request) {
        
        UUID userId = tokenValidationService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto updatedUser = userService.updateUser(userId, request.getName());
        return ResponseEntity.ok(updatedUser);
    }
}
