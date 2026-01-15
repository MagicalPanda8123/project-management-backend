package org.magicalpanda.projectmanagementbackend.repository;

import org.magicalpanda.projectmanagementbackend.model.Project;
import org.magicalpanda.projectmanagementbackend.model.enumeration.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndStatusIn(Long id, Collection<ProjectStatus> statuses);
}
