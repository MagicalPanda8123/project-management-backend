package org.magicalpanda.projectmanagementbackend.service;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.CreateProjectRequest;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
}
