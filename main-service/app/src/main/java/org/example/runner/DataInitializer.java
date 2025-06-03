package org.example.runner;

import java.util.UUID;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByName("Админ-аккаунт").isEmpty()) {
            User admin = new User();
            admin.setId(UUID.fromString("3f9f0c0e-45a3-4b79-8a67-62b4b3a8f0c9"));
            admin.setName("Админ-аккаунт");
            admin.setRole(User.Role.valueOf("ADMIN"));
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
            System.out.println("Admin user created");
        }
    }
}
