package org.magicalpanda.projectmanagementbackend.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.response.LoginResponse;
import org.magicalpanda.projectmanagementbackend.model.AuthIdentity;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.enumeration.AuthProvider;
import org.magicalpanda.projectmanagementbackend.repository.AuthIdentityRepository;
import org.magicalpanda.projectmanagementbackend.security.jwt.JwtService;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Oauth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final AuthIdentityRepository authIdentityRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOidcUser oauthUser = (CustomOidcUser) authentication.getPrincipal();
        String providerUserId = oauthUser.getAttribute("sub");

        AuthIdentity identity = authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, providerUserId)
                .orElseThrow();

        SecurityUser principal = oauthUser.getSecurityUser();

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(identity.getUser());

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build()
                )
        );
    }
}
