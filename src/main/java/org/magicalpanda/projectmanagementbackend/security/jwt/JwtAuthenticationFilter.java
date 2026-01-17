package org.magicalpanda.projectmanagementbackend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.model.enumeration.Role;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.magicalpanda.projectmanagementbackend.service.TokenBlacklistService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 1. Validate access token and retrieve claims
            Claims claims = jwtService.validateAccessToken(token);

            // 2. Blacklist check
            String jti = claims.get("jti", String.class);
            if (tokenBlacklistService.isBlacklisted(jti)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 3. Rebuild SecurityUser
            SecurityUser principal = buildPrincipal(claims);

            // 4. Create Authentication
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            // 5. Populate SecurityContext
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);

    }

    private SecurityUser buildPrincipal(Claims claims) {

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);

        return new SecurityUser(
                userId,
                username,
                email,
                Role.valueOf(role),
                true
        );
    }

}
