package org.example.integration;

import org.example.dto.user.UpdateUserRequest;
import org.example.dto.user.UserDto;
import org.example.entity.User;
import org.example.repository.UserRepository;
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


import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

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
    private UserRepository userRepository;

    @MockBean
    private TokenValidationService tokenValidationService;

    private UUID userId;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        var user = new User();
        user.setName("Original Name");
        user.setEmail("user@example.com");
        user.setRole(User.Role.valueOf("USER"));
        user.setId(UUID.fromString("7c850c02-4cee-46f5-b16f-29349c0cf563"));
        userId = user.getId();
        userRepository.save(user);
        when(tokenValidationService.getUserIdFromToken("mock-token")).thenReturn(userId);
    }

    @Test
    void testGetCurrentUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "mock-token");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.GET,
                request,
                UserDto.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Original Name", response.getBody().getName());
        assertEquals("user@example.com", response.getBody().getEmail());
    }

    @Test
    void testUpdateCurrentUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "mock-token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");

        HttpEntity<UpdateUserRequest> request = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.PUT,
                request,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getName());

        var updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals("Updated Name", updatedUser.getName());
    }
}
