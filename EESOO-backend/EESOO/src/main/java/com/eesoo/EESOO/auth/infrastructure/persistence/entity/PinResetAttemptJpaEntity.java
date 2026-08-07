package com.eesoo.EESOO.auth.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pin_reset_attempts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pin_reset_attempt_user_device", columnNames = {
                "user_id",
                "device_install_id"
        })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PinResetAttemptJpaEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "device_install_id", nullable = false, columnDefinition = "uuid")
    private UUID deviceInstallId;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "mobile_confirmed_at")
    private Instant mobileConfirmedAt;

    @Column(name = "pin_issued_at")
    private Instant pinIssuedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}