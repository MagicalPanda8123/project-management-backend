package org.magicalpanda.projectmanagementbackend.service;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.CreateProjectRequest;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectDetailsResponse;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectResponse;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectSummaryResponse;
import org.magicalpanda.projectmanagementbackend.exception.ResourceNotFoundException;
import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.magicalpanda.projectmanagementbackend.model.Project;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectStatus;
import org.magicalpanda.projectmanagementbackend.repository.MembershipRepository;
import org.magicalpanda.projectmanagementbackend.repository.ProjectRepository;
import org.magicalpanda.projectmanagementbackend.repository.UserRepository;
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

    public Page<ProjectSummaryResponse> getMyProjects(Long userId, String scope, Pageable pageable) {
        Page<Membership> memberships;

        // set default scope to "all"
        String resolvedScope = (scope == null) ? "all" : scope.toLowerCase();

        memberships = switch (resolvedScope) {

            case "owned" -> membershipRepository
                    .findByUserIdAndRoleAndStatus(
                            userId,
                            ProjectRole.OWNER,
                            MembershipStatus.ACTIVE,
                            pageable
                    );

            case "member" -> membershipRepository
                    .findByUserIdAndRoleInAndStatus(
                            userId,
                            List.of(ProjectRole.MANAGER, ProjectRole.MEMBER),
                            MembershipStatus.ACTIVE,
                            pageable
                    );

            case "all" -> membershipRepository
                    .findByUserIdAndStatus(
                            userId,
                            MembershipStatus.ACTIVE,
                            pageable
                    );

            default -> throw new IllegalArgumentException("Invalid scope provided: " + scope + ", possible values: all, owned, member");
        };

        return memberships.map(this::toProjectSummary);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@projectPolicy.canViewProject(#projectId, #userId)")
    public ProjectDetailsResponse getProjectDetails(Long projectId, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

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
