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
    private final boolean emailVerified;

    @Getter
    private final Role role;

    public SecurityUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.emailVerified = user.isEmailVerified();
        this.role = user.getRole();
    }

    public SecurityUser(Long userId, String username, String email, Role role, boolean emailVerified) {
        this.id = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }


    /**
     * Credentials are not exposed via UserDetails.
     * Authentication already happened upstream.
     */
    @Override
    public @Nullable String getPassword() {
        return null;
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

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }
}
