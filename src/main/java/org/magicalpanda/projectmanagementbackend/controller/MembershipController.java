package org.magicalpanda.projectmanagementbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.CreateMembershipRequest;
import org.magicalpanda.projectmanagementbackend.dto.response.MembershipResponse;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.magicalpanda.projectmanagementbackend.service.MembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping
    public ResponseEntity<MembershipResponse> inviteMember(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateMembershipRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        MembershipResponse response = membershipService.createMembership(projectId, securityUser.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
