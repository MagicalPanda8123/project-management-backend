package org.magicalpanda.projectmanagementbackend.dto.response;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

public final class SecurityErrorResponse {

    public static ApiErrorResponse forbidden(HttpServletRequest request) {
        return ApiErrorResponse.builder()
                .status(403)
                .error("Forbidden")
                .message("You do not have permission to perform this action")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
    }

    public static ApiErrorResponse unauthorized(HttpServletRequest request) {
        return ApiErrorResponse.builder()
                .status(401)
                .error("Unauthorized")
                .message("Authentication is required to access this resource")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
    }
}
