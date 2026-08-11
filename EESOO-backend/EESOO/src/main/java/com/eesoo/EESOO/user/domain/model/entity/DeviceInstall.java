package com.eesoo.EESOO.user.domain.model.entity;

import java.time.Instant;
import java.util.Objects;

import com.eesoo.EESOO.user.domain.model.enums.DeviceIdNullableReason;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.InstallId;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;

public class DeviceInstall {

    private final DeviceInstallId id;

    private final DeviceId deviceId;
    private final InstallId installId;

    private final String osVersion;
    private final String platform;
    private final DeviceIdNullableReason deviceIdNullableReason;

    private final Instant firstSeenAt;
    private final Instant lastSeenAt;

    private DeviceInstall(
            DeviceInstallId id,
            DeviceId deviceIdOrNull,
            InstallId installId,
            String osVersion,
            String platform,
            DeviceIdNullableReason deviceIdNullableReasonOrNull,
            Instant firstSeenAt,
            Instant lastSeenAt
    ) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (installId == null) throw new IllegalArgumentException("installId cannot be null");
        if (isBlank(osVersion)) throw new IllegalArgumentException("osVersion cannot be null/blank");
        if (isBlank(platform)) throw new IllegalArgumentException("platform cannot be null/blank");
        if (firstSeenAt == null) throw new IllegalArgumentException("firstSeenAt cannot be null");
        if (lastSeenAt == null) throw new IllegalArgumentException("lastSeenAt cannot be null");

        DeviceIdNullableReason storedReason = normalizeReason(deviceIdOrNull, deviceIdNullableReasonOrNull);

        this.id = id;
        this.deviceId = deviceIdOrNull;
        this.installId = installId;
        this.osVersion = osVersion.trim();
        this.platform = platform.trim();
        this.deviceIdNullableReason = storedReason;
        this.firstSeenAt = firstSeenAt;
        this.lastSeenAt = lastSeenAt;
    }

    public static DeviceInstall initializeOnFirstSeen(
            DeviceId deviceIdOrNull,
            InstallId installId,
            String osVersion,
            String platform,
            DeviceIdNullableReason deviceIdNullableReasonOrNull,
            TimeProvider timeProvider
    ) {
        Instant now = timeProvider.now();
        return new DeviceInstall(
                DeviceInstallId.create(),
                deviceIdOrNull,
                installId,
                osVersion,
                platform,
                deviceIdNullableReasonOrNull,
                now,
                now
        );
    }

    public static DeviceInstall rehydrate(
            DeviceInstallId id,
            DeviceId deviceIdOrNull,
            InstallId installId,
            String osVersion,
            String platform,
            DeviceIdNullableReason deviceIdNullableReasonOrNull,
            Instant firstSeenAt,
            Instant lastSeenAt
    ) {
        return new DeviceInstall(
                id,
                deviceIdOrNull,
                installId,
                osVersion,
                platform,
                deviceIdNullableReasonOrNull,
                firstSeenAt,
                lastSeenAt
        );
    }

    private static DeviceIdNullableReason normalizeReason(
            DeviceId deviceIdOrNull,
            DeviceIdNullableReason reasonOrNull
    ) {
        if (deviceIdOrNull != null) {
            return null;
        }
        return reasonOrNull != null ? reasonOrNull : DeviceIdNullableReason.UNKNOWN;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public DeviceInstall attachDeviceId(DeviceId newDeviceId) {
        if (newDeviceId == null) {
            throw new IllegalArgumentException("deviceId cannot be null when attaching");
        }

        if (this.deviceId == null) {
            return new DeviceInstall(
                    this.id,
                    newDeviceId,
                    this.installId,
                    this.osVersion,
                    this.platform,
                    null,
                    this.firstSeenAt,
                    this.lastSeenAt
            );
        }

        if (!this.deviceId.equals(newDeviceId)) {
            throw new IllegalStateException(
                    "DeviceId mismatch for same installId. installId=" + installId +
                    ", existingDeviceId=" + this.deviceId +
                    ", newDeviceId=" + newDeviceId
            );
        }

        return this;
    }

    public DeviceInstall withNewInstallId(InstallId newInstallId) {
        if (newInstallId == null) {
            throw new IllegalArgumentException("installId cannot be null");
        }

        return new DeviceInstall(
                this.id,
                this.deviceId,
                newInstallId,
                this.osVersion,
                this.platform,
                this.deviceIdNullableReason,
                this.firstSeenAt,
                this.lastSeenAt
        );
    }

    public DeviceInstall touch(
            String osVersion,
            String platform,
            DeviceIdNullableReason deviceIdNullableReasonOrNull,
            TimeProvider timeProvider
    ) {
        return new DeviceInstall(
                this.id,
                this.deviceId,
                this.installId,
                osVersion,
                platform,
                deviceIdNullableReasonOrNull,
                this.firstSeenAt,
                timeProvider.now()
        );
    }

    public DeviceInstallId getId() {
        return id;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public InstallId getInstallId() {
        return installId;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public DeviceIdNullableReason getDeviceIdNullableReason() {
        return deviceIdNullableReason;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceInstall)) return false;
        DeviceInstall that = (DeviceInstall) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
