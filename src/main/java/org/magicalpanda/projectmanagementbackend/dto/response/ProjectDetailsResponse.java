package org.magicalpanda.projectmanagementbackend.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;

import java.time.Instant;

@Getter
@Builder
public class ProjectDetailsResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final Instant createdAt;
    private final ProjectRole myRole;
    private final Instant joinedAt;
}
