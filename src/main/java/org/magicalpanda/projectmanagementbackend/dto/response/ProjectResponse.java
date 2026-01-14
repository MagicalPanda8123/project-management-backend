package org.magicalpanda.projectmanagementbackend.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectStatus;

import java.time.Instant;

@Getter
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Long ownerId;
    private Instant createdAt;
}
