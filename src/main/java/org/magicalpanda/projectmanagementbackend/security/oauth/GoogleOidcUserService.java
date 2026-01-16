package org.magicalpanda.projectmanagementbackend.security.oauth;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.model.AuthIdentity;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleOidcUserService extends OidcUserService {

    private final OAuthIdentityProvisioningService provisioningService;

    /**
     *
     * This method loadUser() is invoked by Spring Security after obtaining the access token to the RS
     * to build the Authentication instance, after which the success handler will be triggerred with
     * this Authentication we set here.
     *
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        // Triggers token validation + claims extraction
        OidcUser oidcUser = super.loadUser(userRequest);

        // Pulls the provicer key from config (application.yml)
        String provider = userRequest
                .getClientRegistration()
                .getRegistrationId();

        // For now, support only 1 IdP - Google
        if (!"google".equals(provider)) {
            throw new OAuth2AuthenticationException(
                    "Unsupported OIDC provider: " + provider
            );
        }

        // Extracts OIDC-guaranteed claims
        String providerUserId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String givenName = oidcUser.getGivenName();
        String familyName = oidcUser.getFamilyName();

        // Provision the AuthIdentity (either create new or getting existing one)
        AuthIdentity identity = provisioningService.provisionGoogleUser(
                providerUserId,
                email,
                givenName,
                familyName
        );

        SecurityUser securityUser =
                new SecurityUser(
                        identity.getUser().getId(),
                        identity.getUser().getUsername(),
                        identity.getUser().getEmail(),
                        identity.getUser().getRole(),
                        identity.getUser().isEmailVerified()
                );

        // Custom OIDC User also contains SecurityUser
        return new CustomOidcUser(
                new DefaultOidcUser(
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + securityUser.getRole().name()
                                )
                        ),
                        oidcUser.getIdToken(),
                        oidcUser.getUserInfo(),
                        "sub"
                ),
                securityUser
        );
    }

}
