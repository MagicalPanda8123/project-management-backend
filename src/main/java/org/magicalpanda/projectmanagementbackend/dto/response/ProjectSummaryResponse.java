package org.magicalpanda.projectmanagementbackend.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectStatus;

import java.time.Instant;

@Getter
@Builder
public class ProjectSummaryResponse {

    private final Long id;
    private final String name;
    private final ProjectRole role; // role of current user against the project
    private final ProjectStatus status;
    private final Instant createdAt;
}
