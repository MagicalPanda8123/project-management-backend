package org.magicalpanda.projectmanagementbackend.repository;

import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.magicalpanda.projectmanagementbackend.model.enumeration.MembershipStatus;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUserIdAndStatus(Long userId, MembershipStatus status);

    List<Membership> findByUserIdAndRoleAndStatus(Long userId, ProjectRole role, MembershipStatus status);

    List<Membership> findByUserIdAndRoleInAndStatus(Long userId, Collection<ProjectRole> roles, MembershipStatus status);
}
