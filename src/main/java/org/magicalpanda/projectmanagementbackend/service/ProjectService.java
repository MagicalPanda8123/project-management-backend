package org.magicalpanda.projectmanagementbackend.service;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.enumeration.ProjectStatusFilter;
import org.magicalpanda.projectmanagementbackend.dto.request.CreateProjectRequest;
import org.magicalpanda.projectmanagementbackend.dto.request.UpdateProjectRequest;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectDetailsResponse;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectResponse;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectSummaryResponse;
import org.magicalpanda.projectmanagementbackend.exception.InvalidStateTransition;
import org.magicalpanda.projectmanagementbackend.exception.ResourceNotFoundException;
import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.magicalpanda.projectmanagementbackend.model.Project;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectStatus;
import org.magicalpanda.projectmanagementbackend.policy.ProjectPolicy;
import org.magicalpanda.projectmanagementbackend.repository.MembershipRepository;
import org.magicalpanda.projectmanagementbackend.repository.ProjectRepository;
import org.magicalpanda.projectmanagementbackend.repository.UserRepository;
import org.magicalpanda.projectmanagementbackend.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(CreateProjectRequest request, Long userId) {

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        // 1. Create project
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .status(ProjectStatus.IN_PROGRESS)
                .build();

        projectRepository.save(project);

        // 2. Create membership
        Membership ownerMembership = Membership.builder()
                .user(owner)
                .project(project)
                .role(ProjectRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .JoinedAt(Instant.now())
                .build();

        membershipRepository.save(ownerMembership);

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .ownerId(owner.getId())
                .createdAt(project.getCreatedAt())
                .build();
    }

    public Page<ProjectSummaryResponse> getMyProjects(Long userId, String scope, List<ProjectStatusFilter> status, Pageable pageable) {

        Page<Membership> memberships;
        List<ProjectStatus> resolvedStatus = ProjectPolicy.resolveVisibleStatuses(status);

        // set default scope to "all"
        String resolvedScope = (scope == null) ? "all" : scope.toLowerCase();

        memberships = switch (resolvedScope) {

            case "owned" -> membershipRepository
                    .findByUserIdAndRoleAndStatusAndProject_StatusIn(
                            userId,
                            ProjectRole.OWNER,
                            MembershipStatus.ACTIVE,
                            resolvedStatus,
                            pageable
                    );

            case "member" -> membershipRepository
                    .findByUserIdAndRoleInAndStatusAndProject_StatusIn(
                            userId,
                            List.of(ProjectRole.MANAGER, ProjectRole.MEMBER),
                            MembershipStatus.ACTIVE,
                            resolvedStatus,
                            pageable
                    );

            case "all" -> membershipRepository
                    .findByUserIdAndStatusAndProject_StatusIn(
                            userId,
                            MembershipStatus.ACTIVE,
                            resolvedStatus,
                            pageable
                    );

            default -> throw new IllegalArgumentException("Invalid scope provided: " + scope + ", possible values: all, owned, member");
        };

        return memberships.map(this::toProjectSummary);
    }

    @Transactional(readOnly = true)
    // Only for ADMIN or active members (OWNER, MANGER, MEMBER)
    @PreAuthorize("@projectPolicy.canViewProject(#projectId, #userId)")
    public ProjectDetailsResponse getProjectDetails(Long projectId, Long userId) {
        Project project;

        if (SecurityUtils.isAdmin()) {
            // Admins can view project details regardless of the project status
            project =  projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        } else {
            // Non-admins can view project details with restricted statuses (archived or in progress only)
            project = projectRepository
                    .findByIdAndStatusIn(
                            projectId,
                            List.of(
                                    ProjectStatus.IN_PROGRESS,
                                    ProjectStatus.COMPLETED,
                                    ProjectStatus.ARCHIVED
                            ))
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        }

        Membership membership = membershipRepository
                .findByProjectIdAndUserIdAndStatus(projectId, userId, MembershipStatus.ACTIVE)
                .orElse(null); // ADMIN may not have membership

        ProjectRole myRole = (membership != null) ? membership.getRole() : null;
        Instant joinedAt = (membership != null) ? membership.getJoinedAt() : null;

        return ProjectDetailsResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .myRole(myRole)
                .joinedAt(joinedAt)
                .build();
    }

    // Only for ADMIN or active members (OWNER, MANAGER)
    @PreAuthorize("@projectPolicy.canUpdateProject(#projectId, #userId)")
    public void updateProject(
            Long projectId,
            Long userId,
            UpdateProjectRequest request
    ) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // DELETED projects are permanently immutable
        if (project.getStatus().equals(ProjectStatus.DELETED)) {
            throw new IllegalStateException("The project at its current state " + project.getStatus() + " cannot be updated");
        }

        // ARCHIVED projects cannot be updated if users don't update the status to other states as well
        if (project.getStatus().equals(ProjectStatus.ARCHIVED)
                && (request.getStatus() == null || request.getStatus().equals(ProjectStatus.ARCHIVED))) {
            throw new IllegalStateException("The project at its current state " + project.getStatus() + " cannot be updated");
        }

        // Do not allow DELETE project through this method
        if (request.getStatus() != null &&  request.getStatus().equals(ProjectStatus.DELETED)) {
            throw new InvalidStateTransition("Project cannot be deleted through PATCH, use DELETE instead.");
        }

        if (request.getName() != null) {
            project.setName(request.getName());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
    }

    private ProjectSummaryResponse toProjectSummary(Membership membership) {
        Project project = membership.getProject();

        return ProjectSummaryResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .status(project.getStatus())
                .role(membership.getRole())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
