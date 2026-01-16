package org.magicalpanda.projectmanagementbackend.service;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.CreateMembershipRequest;
import org.magicalpanda.projectmanagementbackend.dto.request.UpdateMembershipRequest;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
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

        Membership membership = membershipRepository.findByProjectIdAndUserId(projectId, request.getUserId()).orElse(null);

        if (membership != null) {
            if (membership.getStatus().equals(MembershipStatus.ACTIVE) || membership.getStatus().equals(MembershipStatus.PENDING)) {
                throw new IllegalStateException("An active/pending membership already exists !");
            }
            membership.setStatus(MembershipStatus.PENDING);
            membershipRepository.save(membership);

            return MembershipResponse.from(membership);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        membership = Membership.builder()
                .user(user)
                .project(project)
                .role(ProjectRole.MEMBER)
                .status(MembershipStatus.PENDING)
                .build();

        membershipRepository.save(membership);

        return MembershipResponse.from(membership);
    }

    @PreAuthorize("@membershipPolicy.canUpdate(#membershipId, #actorId)")
    public void updateMembership(Long membershipId, Long actorId, UpdateMembershipRequest request) {

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));

        Membership actorMembership = membershipRepository
                .findByUserIdAndProjectId(
                        actorId,
                        membership.getProject().getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("No membership against this found project for actor's id: " + actorId));

        boolean isSelf = actorId.equals(membership.getUser().getId());

        // If status is provided, update (if valid)
        if (request.getStatus() != null) {
            validateStatusUpdate(isSelf, actorMembership);
            membership.setStatus(request.getStatus());
        }

        if (request.getRole() != null) {
            validateRoleUpdate(actorMembership, request.getRole());
            membership.setRole(request.getRole());
        }
    }

    private void validateStatusUpdate(boolean isSelf, Membership actorMembership) {
        if (isSelf) {
            return;
        }

        ProjectRole actorRole = actorMembership.getRole();

        if (actorRole != ProjectRole.MANAGER && actorRole != ProjectRole.OWNER) {
            throw new AccessDeniedException("Only manager or owner can update member status");
        }
    }

    private void validateRoleUpdate(Membership actorMembership, ProjectRole newRole) {
        if (actorMembership.getRole() != ProjectRole.OWNER) {
            throw new AuthorizationDeniedException("Only owner can update member role");
        }

        if (newRole.equals(ProjectRole.OWNER)) {
            throw new AuthorizationDeniedException("You cannot appoint another owner");
        }
    }


}
