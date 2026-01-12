package org.magicalpanda.projectmanagementbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.magicalpanda.projectmanagementbackend.model.enumeration.VerificationPurpose;

import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Table(name = "verification_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VerificationCode {

    private static final Random RANDOM = new Random();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    private VerificationPurpose purpose;

    @ManyToOne(optional = false)
    private User user;

    @Column(name = "is_used")
    @Setter
    private boolean isUsed;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
