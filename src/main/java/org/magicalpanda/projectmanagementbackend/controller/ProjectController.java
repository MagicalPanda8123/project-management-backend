package org.magicalpanda.projectmanagementbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.CreateProjectRequest;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectResponse;
import org.magicalpanda.projectmanagementbackend.dto.response.ProjectSummaryResponse;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.magicalpanda.projectmanagementbackend.service.ProjectService;
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
    public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects(
            @RequestParam(required = false, defaultValue = "all") String scope,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        List<ProjectSummaryResponse> projects = projectService.getMyProjects(securityUser.getId(), scope);

        return ResponseEntity.ok(projects);
    }
}
