package org.magicalpanda.projectmanagementbackend.repository;

import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Page<Membership> findByUserIdAndStatusAndProject_StatusIn(
            Long userId,
            MembershipStatus status,
            Collection<ProjectStatus> projectStatuses,
            Pageable pageable
    );

    Page<Membership> findByUserIdAndRoleAndStatusAndProject_StatusIn(
            Long userId,
            ProjectRole role,
            MembershipStatus status,
            Collection<ProjectStatus> projectStatuses,
            Pageable pageable
    );

    Page<Membership> findByUserIdAndRoleInAndStatusAndProject_StatusIn(
            Long userId,
            Collection<ProjectRole> roles,
            MembershipStatus status,
            Collection<ProjectStatus> projectStatuses,
            Pageable pageable
    );

    Optional<Membership> findByUserIdAndProjectId(Long userId, Long projectId);

    boolean existsByProjectIdAndUserIdAndRoleInAndStatus(Long projectId, Long userId, Collection<ProjectRole> roles, MembershipStatus status);

    Optional<Membership> findByProjectIdAndUserId(Long projectId, Long userId);

    Optional<Membership> findByProjectIdAndUserIdAndStatus(Long projectId, Long userId, MembershipStatus status);
}
