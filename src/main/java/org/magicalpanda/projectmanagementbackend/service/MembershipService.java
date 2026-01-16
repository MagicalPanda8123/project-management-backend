package org.magicalpanda.projectmanagementbackend.service;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.CreateMembershipRequest;
import org.magicalpanda.projectmanagementbackend.dto.response.MembershipResponse;
import org.magicalpanda.projectmanagementbackend.exception.ResourceNotFoundException;
import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.magicalpanda.projectmanagementbackend.model.Project;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.magicalpanda.projectmanagementbackend.repository.MembershipRepository;
import org.magicalpanda.projectmanagementbackend.repository.ProjectRepository;
import org.magicalpanda.projectmanagementbackend.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // Only ADMIN or project owner, (active) manager can invite members
    @PreAuthorize("@membershipPolicy.canInvite(#projectId, #actorId)")
    public MembershipResponse createMembership(
            Long projectId,
            Long actorId,
            CreateMembershipRequest request
    ) {
        if (membershipRepository.existsByProjectIdAndUserIdAndRoleInAndStatus(
                projectId,
                request.getUserId(),
                List.of(ProjectRole.OWNER, ProjectRole.MANAGER, ProjectRole.MEMBER),
                MembershipStatus.ACTIVE
        )) {
            throw new IllegalStateException("An active membership already exists !");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        Membership membership = Membership.builder()
                .user(user)
                .project(project)
                .role(ProjectRole.MEMBER)
                .status(MembershipStatus.PENDING)
                .build();

        membershipRepository.save(membership);

        return MembershipResponse.from(membership);
    }
}
