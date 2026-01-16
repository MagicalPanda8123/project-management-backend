package org.magicalpanda.projectmanagementbackend.policy;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.exception.ResourceNotFoundException;
import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.magicalpanda.projectmanagementbackend.repository.MembershipRepository;
import org.magicalpanda.projectmanagementbackend.util.SecurityUtils;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("membershipPolicy")
@RequiredArgsConstructor
public class MembershipPolicy {

    private final MembershipRepository membershipRepository;

    public boolean canInvite(Long projectId, Long userId) {

        if (SecurityUtils.isAdmin()) {
            return true;
        }

        boolean allowed = membershipRepository
                .existsByProjectIdAndUserIdAndRoleInAndStatus(
                        projectId,
                        userId,
                        List.of(ProjectRole.OWNER, ProjectRole.MANAGER),
                        MembershipStatus.ACTIVE
                );

        if (!allowed) {
            throw new AuthorizationDeniedException(
                    "You ain't no owner or manager to invite members </3"
            );
        }

        return true;
    }

    public boolean canUpdate(Long membershipId, Long actorId) {

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));

        if (membership.getStatus().equals(MembershipStatus.LEFT) || membership.getStatus().equals(MembershipStatus.DELETED)) {
            throw new AuthorizationDeniedException("Membership is in invalid state for mutation, current status: " +  membership.getStatus().name());
        }

        Optional<Membership> result = membershipRepository
                .findByUserIdAndProjectId(
                        actorId,
                        membership.getProject().getId()
                );

        if (result.isEmpty()) {
            throw new AuthorizationDeniedException("You're not a member of this project to perform the action!");
        }

        Membership actorMembership = result.get();

        if (!actorMembership.getStatus().equals(MembershipStatus.ACTIVE) && !actorMembership.getStatus().equals(MembershipStatus.PENDING)) {
            throw new AuthorizationDeniedException("You're not a valid member of this project to perform the action!");
        }

        return true;
    }
}
