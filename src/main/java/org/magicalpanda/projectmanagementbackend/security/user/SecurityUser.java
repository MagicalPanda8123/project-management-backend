package org.magicalpanda.projectmanagementbackend.security.user;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.enumeration.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {

    @Getter
    private final Long id;

    @Getter
    private final String email;

    private final String username;
    private final String passwordHash;
    private final boolean emailVerified;

    @Getter
    private final Role role;

    public SecurityUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.emailVerified = user.isEmailVerified();
        this.role = user.getRole();
    }

    public SecurityUser(Long userId, String username, String email, Role role) {
        this.id = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.passwordHash = null;
        this.emailVerified = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public @Nullable String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    // This is where email verification enforced at login.
    @Override
    public boolean isEnabled() {
        return this.emailVerified;
    }
}
