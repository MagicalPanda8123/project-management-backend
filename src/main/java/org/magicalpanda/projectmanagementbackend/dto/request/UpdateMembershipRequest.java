package org.magicalpanda.projectmanagementbackend.dto.request;

import lombok.Getter;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;

@Getter
public class UpdateMembershipRequest {

    private ProjectRole role;

    private MembershipStatus status;
}
