package org.magicalpanda.projectmanagementbackend.repository;

import org.magicalpanda.projectmanagementbackend.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
}
