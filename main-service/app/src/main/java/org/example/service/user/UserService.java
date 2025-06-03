package org.example.service.user;

import org.example.dto.user.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto getUserById(UUID id);
    void createIfNotExists(UserDto dto);
    UserDto updateUser(UUID id, String name);
    
    // методы для админских ручек
    List<UserDto> getAllUsers();
    void adminUpdateUser(UUID id, String name, String role);
}
