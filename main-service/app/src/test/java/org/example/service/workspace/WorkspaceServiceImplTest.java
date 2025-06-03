package org.example.service.workspace;

import org.example.dto.workspace.CreateWorkspaceRequest;
import org.example.dto.workspace.UpdateWorkspaceRequest;
import org.example.dto.workspace.WorkspaceDto;
import org.example.entity.Workspace;
import org.example.exception.WorkspaceNotFoundException;
import org.example.repository.BookingRepository;
import org.example.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkspaceServiceImplTest {

    private WorkspaceRepository workspaceRepository;
    private BookingRepository bookingRepository;
    private WorkspaceServiceImpl workspaceService;

    @BeforeEach
    void setUp() {
        workspaceRepository = mock(WorkspaceRepository.class);
        bookingRepository = mock(BookingRepository.class);
        workspaceService = new WorkspaceServiceImpl(workspaceRepository, bookingRepository);
    }

    @Test
    void testFindAll() {
        Workspace w1 = new Workspace(1L, "Room A", 10, true);
        Workspace w2 = new Workspace(2L, "Room B", 5, false);
        when(workspaceRepository.findAll()).thenReturn(List.of(w1, w2));

        List<WorkspaceDto> result = workspaceService.findAll();

        assertEquals(2, result.size());
        assertEquals("Room A", result.get(0).getName());
        assertEquals("Room B", result.get(1).getName());
    }

    @Test
    void testFindAvailableBetween() {
        Workspace w1 = new Workspace(1L, "Room A", 10, true);
        Workspace w2 = new Workspace(2L, "Room B", 5, true);
        Workspace w3 = new Workspace(3L, "Room C", 7, false);

        when(workspaceRepository.findAll()).thenReturn(List.of(w1, w2, w3));
        when(bookingRepository.findBusyWorkspaceIdsBetween(any(), any())).thenReturn(List.of(2L));

        List<WorkspaceDto> result = workspaceService.findAvailableBetween(LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        assertEquals(1, result.size());
        assertEquals("Room A", result.get(0).getName());
    }

    @Test
    void testCreateWorkspace() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("Room X");
        request.setCapacity(15);
        Workspace saved = new Workspace(1L, "Room X", 15, true);

        when(workspaceRepository.save(any())).thenReturn(saved);

        WorkspaceDto result = workspaceService.createWorkspace(request);

        assertEquals("Room X", result.getName());
        assertEquals(15, result.getCapacity());
        assertTrue(result.isActive());
    }

    @Test
    void testUpdateWorkspace_Success() {
        Workspace existing = new Workspace(1L, "Old Name", 10, true);
        UpdateWorkspaceRequest request = new UpdateWorkspaceRequest();
        request.setName("New Name");
        request.setCapacity(20);
        request.setActive(false);
        Workspace updated = new Workspace(1L, "New Name", 20, false);

        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(workspaceRepository.save(any())).thenReturn(updated);

        WorkspaceDto result = workspaceService.updateWorkspace(1L, request);

        assertEquals("New Name", result.getName());
        assertEquals(20, result.getCapacity());
        assertFalse(result.isActive());
    }

    @Test
    void testUpdateWorkspace_NotFound() {
        UpdateWorkspaceRequest request = new UpdateWorkspaceRequest();
        request.setName("Name");
        request.setCapacity(5);
        request.setActive(true);
        when(workspaceRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.updateWorkspace(42L, request));
    }

    @Test
    void testDeactivateWorkspace_Success() {
        Workspace workspace = new Workspace(1L, "Room", 10, true);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));

        workspaceService.deactivateWorkspace(1L);

        assertFalse(workspace.isActive());
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void testDeactivateWorkspace_NotFound() {
        when(workspaceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.deactivateWorkspace(99L));
    }

    @Test
    void testGetById_Success() {
        Workspace workspace = new Workspace(1L, "Room A", 8, true);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));

        WorkspaceDto result = workspaceService.getById(1L);

        assertEquals("Room A", result.getName());
        assertEquals(8, result.getCapacity());
    }

    @Test
    void testGetById_NotFound() {
        when(workspaceRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> workspaceService.getById(404L));
    }

    @Test
    void testDeleteWorkspace_Success() {
        when(workspaceRepository.existsById(1L)).thenReturn(true);

        workspaceService.deleteWorkspace(1L);

        verify(workspaceRepository).deleteById(1L);
    }

    @Test
    void testDeleteWorkspace_NotFound() {
        when(workspaceRepository.existsById(1L)).thenReturn(false);

        assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.deleteWorkspace(1L));
    }

    @Test
    void testUpdateStatus_Success() {
        Workspace workspace = new Workspace(1L, "Room A", 5, false);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any())).thenReturn(workspace);

        WorkspaceDto result = workspaceService.updateStatus(1L, true);

        assertTrue(result.isActive());
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void testUpdateStatus_NotFound() {
        when(workspaceRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.updateStatus(5L, true));
    }
}
