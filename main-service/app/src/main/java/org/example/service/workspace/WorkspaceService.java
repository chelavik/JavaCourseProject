package org.example.service.workspace;

import org.example.dto.workspace.CreateWorkspaceRequest;
import org.example.dto.workspace.UpdateWorkspaceRequest;
import org.example.dto.workspace.WorkspaceDto;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkspaceService {
    List<WorkspaceDto> findAll();
    List<WorkspaceDto> findAvailableBetween(LocalDateTime start, LocalDateTime end);
    WorkspaceDto getById(Long id);

    // для админских ручек
    WorkspaceDto createWorkspace(CreateWorkspaceRequest request);
    WorkspaceDto updateWorkspace(Long id, UpdateWorkspaceRequest request);
    void deactivateWorkspace(Long id);
    WorkspaceDto updateStatus(Long id, boolean active);
    

}
