package org.magicalpanda.projectmanagementbackend.policy;


import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.magicalpanda.projectmanagementbackend.repository.MembershipRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("projectPolicy")
@RequiredArgsConstructor
public class ProjectPolicy {

    private final MembershipRepository  membershipRepository;

    public boolean canViewProject(Long projectId, Long userId) {
        System.out.println("IS ADMIN " + isAdmin());
        return isAdmin() || membershipRepository
                .existsByProjectIdAndUserIdAndRoleInAndStatus(
                        projectId,
                        userId,
                        List.of(
                                ProjectRole.OWNER,
                                ProjectRole.MANAGER,
                                ProjectRole.MEMBER
                        ),
                        MembershipStatus.ACTIVE
                );

    }

    private boolean isAdmin() {
        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
