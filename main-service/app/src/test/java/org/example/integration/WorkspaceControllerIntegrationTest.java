package org.example.integration;

import org.example.dto.workspace.WorkspaceDto;
import org.example.entity.Workspace;
import org.example.repository.WorkspaceRepository;
import org.example.service.TokenValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WorkspaceControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @MockBean
    private TokenValidationService tokenValidationService;

    private Long workspaceId;

    @BeforeEach
    void setup() {
        workspaceRepository.deleteAll();

        var workspace = new Workspace();
        workspace.setName("Test Workspace");
        workspace.setCapacity(10);
        workspace = workspaceRepository.save(workspace);
        workspaceId = workspace.getId();

        when(tokenValidationService.validateToken("mock-token")).thenReturn(true);
    }

    @Test
    void testGetAllWorkspaces() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "mock-token");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                "/api/workspaces",
                HttpMethod.GET,
                request,
                List.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetAvailableWorkspaces() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "mock-token");

        LocalDateTime now = LocalDateTime.now();
        String url = String.format("/api/workspaces/available?start=%s&end=%s",
                now.toString(), now.plusHours(1).toString());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                List.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetWorkspaceById() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "mock-token");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<WorkspaceDto> response = restTemplate.exchange(
                "/api/workspaces/" + workspaceId,
                HttpMethod.GET,
                request,
                WorkspaceDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Workspace", response.getBody().getName());
    }

    @Test
    void testGetAllWorkspaces_Unauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "invalid-token");

        when(tokenValidationService.validateToken("invalid-token")).thenReturn(false);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/workspaces",
                HttpMethod.GET,
                request,
                Void.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}