package org.magicalpanda.projectmanagementbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.enumeration.ProjectStatusFilter;
import org.magicalpanda.projectmanagementbackend.dto.request.CreateProjectRequest;
import org.magicalpanda.projectmanagementbackend.dto.request.UpdateProjectRequest;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectDetailsResponse;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectResponse;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectSummaryResponse;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.magicalpanda.projectmanagementbackend.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        ProjectResponse response = projectService.createProject(request, securityUser.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectSummaryResponse>> getMyProjects(
            @RequestParam(required = false, defaultValue = "all") String scope,
            @RequestParam(required = false) List<ProjectStatusFilter> status,
            @PageableDefault(size =  10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        Page<ProjectSummaryResponse> projects = projectService.getMyProjects(securityUser.getId(), scope, status, pageable);

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsResponse> getProjectById(
            @PathVariable Long projectId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        ProjectDetailsResponse response = projectService.getProjectDetails(projectId, securityUser.getId());
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{projectId}")
    public ResponseEntity<Void> updateProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody UpdateProjectRequest request

    ) {
        projectService.updateProject(projectId, securityUser.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
