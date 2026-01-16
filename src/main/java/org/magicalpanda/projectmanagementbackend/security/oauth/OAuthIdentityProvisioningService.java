package org.magicalpanda.projectmanagementbackend.security.oauth;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.model.AuthIdentity;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.enumeration.AuthProvider;
import org.magicalpanda.projectmanagementbackend.model.enumeration.Role;
import org.magicalpanda.projectmanagementbackend.repository.AuthIdentityRepository;
import org.magicalpanda.projectmanagementbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthIdentityProvisioningService {

    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;

    @Transactional
    public AuthIdentity provisionGoogleUser(
            String providerUserId,
            String email,
            String firstName,
            String lastName
    ) {
        User user = userRepository
                .findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .username(UUID.randomUUID() + lastName)
                                .firstName(firstName)
                                .lastName(lastName)
                                .isEmailVerified(true)
                                .role(Role.USER)
                                .build()
                ));

        return authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, providerUserId)
                .orElseGet(() ->
                        authIdentityRepository.save(
                                AuthIdentity.builder()
                                        .provider(AuthProvider.GOOGLE)
                                        .providerUserId(providerUserId)
                                        .user(user)
                                        .build()
                        )
                );
    }
}
