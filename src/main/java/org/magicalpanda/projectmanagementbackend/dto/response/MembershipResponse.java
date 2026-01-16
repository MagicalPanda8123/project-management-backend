package org.magicalpanda.projectmanagementbackend.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;

@Getter
@Builder
public class MembershipResponse {
    private final Long id;
    private final Long projectId;
    private final Long userId;
    private final ProjectRole role;
    private final MembershipStatus status;

    public static MembershipResponse from(Membership membership) {
        return MembershipResponse.builder()
                .id(membership.getId())
                .projectId(membership.getProject().getId())
                .userId(membership.getUser().getId())
                .role(membership.getRole())
                .status(membership.getStatus())
                .build();
    }
}
