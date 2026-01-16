package org.magicalpanda.projectmanagementbackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectStatus;

@Getter
public class UpdateProjectRequest {

    @Size(min = 1, max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    private ProjectStatus status;
}
