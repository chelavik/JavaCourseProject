package org.example.service.user;

import lombok.RequiredArgsConstructor;

import org.example.dto.user.UserDto;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToDto(user);
    }

    @Override
    public void createIfNotExists(UserDto dto) {
        userRepository.findById(dto.getId()).orElseGet(() -> {
            User user = User.builder()
                    .id(dto.getId())
                    .email(dto.getEmail())
                    .name(dto.getName())
                    .role(User.Role.valueOf(dto.getRole().toUpperCase()))
                    .build();
            return userRepository.save(user);
        });
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void adminUpdateUser(UUID id, String name, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (name != null) user.setName(name);
        if (role != null) user.setRole(User.Role.valueOf(role.toUpperCase()));

        userRepository.save(user);
    }



    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public UserDto updateUser(UUID userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (name != null && !name.isBlank()) user.setName(name);
        userRepository.save(user);

        return mapToDto(user);
    }
}
