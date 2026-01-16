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

    // For remove, change role
    public boolean canManageMembers(Long projectId, Long userId) {

        if (SecurityUtils.isAdmin()) {
            return true;
        }

        boolean allowed = membershipRepository
                .existsByProjectIdAndUserIdAndRoleInAndStatus(
                        projectId,
                        userId,
                        List.of(ProjectRole.OWNER),
                        MembershipStatus.ACTIVE
                );

        if (!allowed) {
            throw new AuthorizationDeniedException(
                    "You ain't no owner to manage members </3"
            );
        }

        return true;
    }

    // Self-action
    public boolean canRespondToInvite(Long membershipId, Long userId) {

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Membership not found")
                );

        boolean allowed = membership.getUser().getId().equals(userId);

        if (!allowed) {
            throw new AuthorizationDeniedException("You are not the invited !");
        }

        return true;
    }

    public boolean canLeave(Long membershipId, Long userId) {

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Membership not found")
                );

        if (!membership.getUser().getId().equals(userId)) {
            throw new AuthorizationDeniedException("You're not this user with id " + membership.getUser().getId() + " to leave");
        }

        if (membership.getRole().equals(ProjectRole.OWNER)) {
            throw new AuthorizationDeniedException(
                    "Owners can't leave the project like that </3"
            );
        }

        return true;
    }
}
