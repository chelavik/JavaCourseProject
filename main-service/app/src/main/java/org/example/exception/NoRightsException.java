package org.example.exception;

import java.util.UUID;

public class NoRightsException extends ApiException {
    public NoRightsException(UUID userId) {
        super("Role of user with id " + userId + " has no rights for this action.");
    }
}
