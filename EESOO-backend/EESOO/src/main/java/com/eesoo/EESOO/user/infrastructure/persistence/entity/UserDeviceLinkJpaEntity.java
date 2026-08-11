package com.eesoo.EESOO.user.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_device_links")
@Getter
@Setter
@NoArgsConstructor
public class UserDeviceLinkJpaEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "device_install_id", nullable = false, columnDefinition = "uuid")
    private UUID deviceInstallId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "linked_device_type", nullable = false)
    private String linkedDeviceType;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    @Column(name = "unlinked_at")
    private Instant unlinkedAt;

    public UserDeviceLinkJpaEntity(
            UUID id,
            UUID deviceInstallId,
            UUID userId,
            String status,
            String linkedDeviceType,
            Instant linkedAt,
            Instant unlinkedAt
    ) {
        this.id = id;
        this.deviceInstallId = deviceInstallId;
        this.userId = userId;
        this.status = status;
        this.linkedDeviceType = linkedDeviceType;
        this.linkedAt = linkedAt;
        this.unlinkedAt = unlinkedAt;
    }
}
