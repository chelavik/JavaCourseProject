package org.example.service;

import java.util.UUID;

import org.example.dto.user.LoginRequest;
import org.example.exception.TokenHandleException;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Service
public class TokenValidationService {

    @Value("${go.service.url}")
    private String goServiceUrl; // URL Go-сервиса для валидации и создания токенов

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public TokenValidationService(RestTemplate restTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    public String requestToken(String email, String password) {
        String url = goServiceUrl + "/login";
        LoginRequest loginRequest = new LoginRequest(email, password);
        return restTemplate.postForObject(url, loginRequest, String.class);
    }

    public boolean validateToken(String token) {
        String url = goServiceUrl + "/validate";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
        ResponseEntity<TokenValidationResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, TokenValidationResponse.class);

        return response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null
                && Boolean.TRUE.equals(response.getBody().getValid());

        } catch (Exception e) {
            return false; // Token is invalid
        }
    }

    public UUID getUserIdFromToken(String token) {
        String url = goServiceUrl + "/validate";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<TokenValidationResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, TokenValidationResponse.class);
            if (response.getBody() != null) {
                if (Boolean.TRUE.equals(response.getBody().getValid())) {
                    return UUID.fromString(response.getBody().getUserId());
                } else {
                    throw new TokenHandleException("Invalid or expired token");
                }
            } 
            return null;
        }  catch (Exception e) {
            throw new TokenHandleException("Token validation error");
        }
    }

    public String getUserRoleFromToken(String token) {
        UUID userId = getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .map(user -> user.getRole().name())
                .orElseThrow(() -> new TokenHandleException("User not found"));
    }

    public boolean isAdmin(String token) {
        return "ADMIN".equalsIgnoreCase(getUserRoleFromToken(token));
    }

    @Getter
    @Setter
    public static class TokenValidationResponse {
        @JsonProperty("expires_at")
        private Integer expiresAt;
        
        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("valid")
        private Boolean valid; 
    }
}

