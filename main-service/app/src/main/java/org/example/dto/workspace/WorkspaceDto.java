package org.example.dto.workspace;

import lombok.*;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
    public class WorkspaceDto {
        private Long id;
        private String name;
        private int capacity;
        private boolean active;
    }
