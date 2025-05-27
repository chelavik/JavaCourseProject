package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.workspace.WorkspaceDto;
import org.example.service.TokenValidationService;
import org.example.service.workspace.WorkspaceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final TokenValidationService tokenValidationService;

    @GetMapping
    public ResponseEntity<List<WorkspaceDto>> getAllWorkspaces(
            @Valid @RequestHeader("Authorization") String token) {

        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(workspaceService.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<WorkspaceDto>> getAvailableWorkspaces(
            @Valid @RequestHeader("Authorization") String token,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(workspaceService.findAvailableBetween(start, end));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceDto> getWorkspaceById(
            @Valid @RequestHeader("Authorization") String token,
            @PathVariable Long id) {

        if (!tokenValidationService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(workspaceService.getById(id));
    }
}
