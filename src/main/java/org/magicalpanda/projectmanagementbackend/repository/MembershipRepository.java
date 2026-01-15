package org.magicalpanda.projectmanagementbackend.repository;

import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Page<Membership> findByUserIdAndStatus(Long userId, MembershipStatus status, Pageable pageable);

    Page<Membership> findByUserIdAndRoleAndStatus(Long userId, ProjectRole role, MembershipStatus status, Pageable pageable);

    Page<Membership> findByUserIdAndRoleInAndStatus(Long userId, Collection<ProjectRole> roles, MembershipStatus status, Pageable pageable);

    boolean existsByProjectIdAndUserIdAndRoleInAndStatus(Long projectId, Long userId, Collection<ProjectRole> roles, MembershipStatus status);

    Optional<Membership> findByProjectIdAndUserIdAndStatus(Long projectId, Long userId, MembershipStatus status);
}
