package org.example.controller;

import org.example.dto.user.CreateUserRequest;
import org.example.entity.User;
import org.example.exception.TokenHandleException;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/internal")
public class InternalController {

    private final UserRepository userRepository;

    @Value("${internal.secret}")
    private String internalSecret;

    public InternalController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/users")
    public ResponseEntity<Void> createUserFromExternal(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateUserRequest request) {

        // Проверка внутреннего токена
        String expectedHeader = "InternalSecret " + internalSecret;
        System.out.println(expectedHeader + " " + authHeader);
        if (!expectedHeader.equals(authHeader)) {
            throw new TokenHandleException("Forbidden: Invalid internal token");
        }
        System.out.println("совпали");
        // Создание пользователя
        User user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setName("Гость");
        user.setRole(User.Role.valueOf("USER"));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
