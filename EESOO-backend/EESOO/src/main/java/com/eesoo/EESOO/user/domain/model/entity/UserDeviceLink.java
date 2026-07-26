package com.eesoo.EESOO.user.domain.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.eesoo.EESOO.user.domain.model.enums.LinkStatus;
import com.eesoo.EESOO.user.domain.model.enums.LinkedDeviceType;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserDeviceLinkId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;

public class UserDeviceLink {

    private final UserDeviceLinkId id;
    private final DeviceInstallId deviceInstallId;
    private final UserId userId;
    private final LinkStatus status;
    private final LinkedDeviceType linkedDeviceType;
    private final LocalDateTime linkedAt;
    private final LocalDateTime unlinkedAt;

    private UserDeviceLink(
            UserDeviceLinkId id,
            DeviceInstallId deviceInstallId,
            UserId userId,
            LinkStatus status,
            LinkedDeviceType linkedDeviceType,
            LocalDateTime linkedAt,
            LocalDateTime unlinkedAt
    ) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (deviceInstallId == null) throw new IllegalArgumentException("deviceInstallId cannot be null");
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        if (status == null) throw new IllegalArgumentException("status cannot be null");
        if (linkedDeviceType == null) throw new IllegalArgumentException("linkedDeviceType cannot be null");
        if (linkedAt == null) throw new IllegalArgumentException("linkedAt cannot be null");

        if (status == LinkStatus.ACTIVE && unlinkedAt != null) {
            throw new IllegalArgumentException("unlinkedAt must be null when status is ACTIVE");
        }

        if (status == LinkStatus.UNLINKED && unlinkedAt == null) {
            throw new IllegalArgumentException("unlinkedAt is required when status is UNLINKED");
        }

        this.id = id;
        this.deviceInstallId = deviceInstallId;
        this.userId = userId;
        this.status = status;
        this.linkedDeviceType = linkedDeviceType;
        this.linkedAt = linkedAt;
        this.unlinkedAt = unlinkedAt;
    }

    public static UserDeviceLink createActive(
            DeviceInstallId deviceInstallId,
            UserId userId,
            LinkedDeviceType linkedDeviceType,
            TimeProvider timeProvider) {
        LocalDateTime now = timeProvider.now();
        return new UserDeviceLink(
                UserDeviceLinkId.create(),
                deviceInstallId,
                userId,
                LinkStatus.ACTIVE,
                linkedDeviceType,
                now,
                null
        );
    }

    public static UserDeviceLink rehydrate(
            UserDeviceLinkId id,
            DeviceInstallId deviceInstallId,
            UserId userId,
            LinkStatus status,
            LinkedDeviceType linkedDeviceType,
            LocalDateTime linkedAt,
            LocalDateTime unlinkedAt
    ) {
        return new UserDeviceLink(
                id,
                deviceInstallId,
                userId,
                status,
                linkedDeviceType,
                linkedAt,
                unlinkedAt
        );
    }

    public UserDeviceLink unlink(TimeProvider timeProvider) {
        if (this.status == LinkStatus.UNLINKED) {
            return this;
        }

        return new UserDeviceLink(
                this.id,
                this.deviceInstallId,
                this.userId,
                LinkStatus.UNLINKED,
                this.linkedDeviceType,
                this.linkedAt,
                timeProvider.now()
        );
    }

    public UserDeviceLinkId getId() {
        return id;
    }

    public DeviceInstallId getDeviceInstallId() {
        return deviceInstallId;
    }

    public UserId getUserId() {
        return userId;
    }

    public LinkStatus getStatus() {
        return status;
    }

    public LinkedDeviceType getLinkedDeviceType() {
        return linkedDeviceType;
    }

    public LocalDateTime getLinkedAt() {
        return linkedAt;
    }

    public LocalDateTime getUnlinkedAt() {
        return unlinkedAt;
    }

    public boolean isActive() {
        return this.status == LinkStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDeviceLink)) return false;
        UserDeviceLink that = (UserDeviceLink) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
