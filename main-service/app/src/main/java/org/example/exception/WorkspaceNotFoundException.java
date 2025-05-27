package org.example.exception;

public class WorkspaceNotFoundException extends ApiException {
    public WorkspaceNotFoundException(Long workspaceId) {
        super("Workspace with id " + workspaceId + " is not found.");
    }  
}
