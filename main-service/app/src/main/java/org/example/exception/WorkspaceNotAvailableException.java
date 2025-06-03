package org.example.exception;

public class WorkspaceNotAvailableException extends ApiException {
    public WorkspaceNotAvailableException(Long workspaceId) {
        super("Workspace with id " + workspaceId + " is not available for the selected time.");
    }
}