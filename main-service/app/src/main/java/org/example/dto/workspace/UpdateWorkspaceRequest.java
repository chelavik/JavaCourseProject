package org.example.dto.workspace;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWorkspaceRequest {
    @NotBlank(message = "Workspace name must not be empty")
    @Size(max = 100, message = "Workspace name must be at most 100 characters")
    private String name;
    
    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;
    private boolean active;
}
