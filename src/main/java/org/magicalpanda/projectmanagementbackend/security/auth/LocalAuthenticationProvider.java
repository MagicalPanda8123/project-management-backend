package org.magicalpanda.projectmanagementbackend.security.auth;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.magicalpanda.projectmanagementbackend.model.AuthIdentity;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.enumeration.AuthProvider;
import org.magicalpanda.projectmanagementbackend.repository.AuthIdentityRepository;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalAuthenticationProvider implements AuthenticationProvider {

    private final AuthIdentityRepository authIdentityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        // 1. Load LOCAL AuthIdentity
        AuthIdentity identity = authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.LOCAL, username)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid username")
                );

        // 2. Verify password
        if (!passwordEncoder.matches(rawPassword, identity.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password");
        }

        // 3. Build Security principal
        User user = identity.getUser();
        SecurityUser principal = new SecurityUser(user);

        // 4. Return authenticated token
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
