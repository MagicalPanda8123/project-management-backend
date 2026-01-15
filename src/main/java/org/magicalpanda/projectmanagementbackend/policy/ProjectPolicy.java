package org.magicalpanda.projectmanagementbackend.policy;


import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.enumeration.ProjectStatusFilter;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectStatus;
import org.magicalpanda.projectmanagementbackend.repository.MembershipRepository;
import org.magicalpanda.projectmanagementbackend.util.SecurityUtils;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("projectPolicy")
@RequiredArgsConstructor
public class ProjectPolicy {

    private final MembershipRepository  membershipRepository;

    private static final List<ProjectStatus> DEFAULT_VISIBLE_STATUSES =
            List.of(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETED);

    // resolve the "status" query param
    public static List<ProjectStatus> resolveVisibleStatuses(List<ProjectStatusFilter> filters) {

        boolean isAdmin = SecurityUtils.isAdmin();

        // Default behavior: no filter provided
        if (filters == null ||  filters.isEmpty())
            return DEFAULT_VISIBLE_STATUSES;

        // Expand ALL
        if (filters.contains(ProjectStatusFilter.ALL)) {
            if (isAdmin) {
                filters = List.of(ProjectStatusFilter.values());
            } else {
                filters = List.of(ProjectStatusFilter.IN_PROGRESS, ProjectStatusFilter.COMPLETED, ProjectStatusFilter.ARCHIVED);
            }
        }

        // Authorization gate
        if (filters.contains(ProjectStatusFilter.DELETED) && !SecurityUtils.isAdmin()) {
            throw new AuthorizationDeniedException("You ain't an admin to view deleted projects bruh </3");
        }

        // Map API-level enum to domain enum
        return filters.stream()
                .filter(f -> f != ProjectStatusFilter.ALL)
                .map(ProjectStatusFilter::name)
                .map(ProjectStatus::valueOf)
                .toList();

    }

    public boolean canViewProject(Long projectId, Long userId) {
        return SecurityUtils.isAdmin() || membershipRepository
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

}
