package org.example.service.workspace;

import lombok.RequiredArgsConstructor;

import org.example.dto.workspace.CreateWorkspaceRequest;
import org.example.dto.workspace.UpdateWorkspaceRequest;
import org.example.dto.workspace.WorkspaceDto;
import org.example.entity.Workspace;
import org.example.exception.WorkspaceNotFoundException;
import org.example.repository.BookingRepository;
import org.example.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final BookingRepository bookingRepository;

    @Override
    public List<WorkspaceDto> findAll() {
        return workspaceRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<WorkspaceDto> findAvailableBetween(LocalDateTime start, LocalDateTime end) {
        List<Long> busyIds = bookingRepository.findBusyWorkspaceIdsBetween(start, end);
        return workspaceRepository.findAll().stream()
                .filter(w -> w.isActive() && !busyIds.contains(w.getId()))
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public WorkspaceDto createWorkspace(CreateWorkspaceRequest request) {
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .active(true)
                .build();
        return mapToDto(workspaceRepository.save(workspace));
    }

    @Override
    public WorkspaceDto updateWorkspace(Long id, UpdateWorkspaceRequest request) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException(id));
        workspace.setName(request.getName());
        workspace.setCapacity(request.getCapacity());
        workspace.setActive(request.isActive());
        return mapToDto(workspaceRepository.save(workspace));
    }

    @Override
    public void deactivateWorkspace(Long id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException(id));
        workspace.setActive(false);
        workspaceRepository.save(workspace);
    }

    private WorkspaceDto mapToDto(Workspace w) {
        return WorkspaceDto.builder()
                .id(w.getId())
                .name(w.getName())
                .capacity(w.getCapacity())
                .active(w.isActive())
                .build();
    }

    @Override
    public WorkspaceDto getById(Long id) {
        return workspaceRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new IllegalArgumentException("Workspace with ID " + id + " not found"));
    }

    public void deleteWorkspace(Long id) {
        if (!workspaceRepository.existsById(id)) {
            throw new WorkspaceNotFoundException(id);
        }
        workspaceRepository.deleteById(id);
    }

    @Override
    public WorkspaceDto updateStatus(Long id, boolean active) {
        return workspaceRepository.findById(id)
                .map(workspace -> {
                    workspace.setActive(active);
                    workspaceRepository.save(workspace);
                    return mapToDto(workspace);
                })
                .orElseThrow(() -> new WorkspaceNotFoundException(id));
    }

}
