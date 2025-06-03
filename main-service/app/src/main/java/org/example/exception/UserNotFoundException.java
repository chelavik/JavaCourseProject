package org.example.exception;

import java.util.UUID;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(UUID userId) {
        super("User with id " + userId + " is not found.");
    }
}
