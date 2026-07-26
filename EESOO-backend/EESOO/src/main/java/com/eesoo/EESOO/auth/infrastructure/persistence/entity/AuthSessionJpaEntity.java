package com.eesoo.EESOO.auth.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auth_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthSessionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "device_install_id", nullable = false, columnDefinition = "uuid")
    private UUID deviceInstallId;

    @Column(name = "refresh_token_hash", nullable = false, length = 512)
    private String refreshTokenHash;

    
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_rotated_at")
    private Instant lastRotatedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    
}
