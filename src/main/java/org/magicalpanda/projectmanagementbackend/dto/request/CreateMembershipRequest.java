package org.magicalpanda.projectmanagementbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;

@Getter
public class CreateMembershipRequest {

    @NotNull
    private Long userId;

    @NotNull
    private ProjectRole role;
}
