package org.magicalpanda.projectmanagementbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user") // prevent lazy-loading issues
public class RefreshToken {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * JTI (JWT ID) for uniqueness and security.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String jti;

    @Column(name = "is_revoked")
    private boolean isRevoked;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
