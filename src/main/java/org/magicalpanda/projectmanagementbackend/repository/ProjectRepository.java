package org.magicalpanda.projectmanagementbackend.repository;

import org.magicalpanda.projectmanagementbackend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
