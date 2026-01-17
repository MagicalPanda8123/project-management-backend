package org.magicalpanda.projectmanagementbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.*;
import org.magicalpanda.projectmanagementbackend.dto.response.LoginResponse;
import org.magicalpanda.projectmanagementbackend.dto.response.RegisterResponse;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RegisterResponse.from(user));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        authService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid  @RequestBody RefreshTokenRequest request
    ) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        authService.logout(request, authHeader);
        return ResponseEntity.noContent().build();
    }
}
