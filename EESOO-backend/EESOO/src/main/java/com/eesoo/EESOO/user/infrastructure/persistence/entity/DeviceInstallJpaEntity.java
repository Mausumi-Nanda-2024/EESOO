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
@Table(name = "device_installs")
@Getter
@Setter
@NoArgsConstructor
public class DeviceInstallJpaEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "device_id", unique = true)
    private String deviceId;

    @Column(name = "install_id", nullable = false, unique = true)
    private String installId;

    @Column(name = "os_version", nullable = false)
    private String osVersion;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "device_id_nullable_reason")
    private String deviceIdNullableReason;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    public DeviceInstallJpaEntity(
            UUID id,
            String deviceId,
            String installId,
            String osVersion,
            String platform,
            String deviceIdNullableReason,
            Instant firstSeenAt,
            Instant lastSeenAt
    ) {
        this.id = id;
        this.deviceId = deviceId;
        this.installId = installId;
        this.osVersion = osVersion;
        this.platform = platform;
        this.deviceIdNullableReason = deviceIdNullableReason;
        this.firstSeenAt = firstSeenAt;
        this.lastSeenAt = lastSeenAt;
    }
}
