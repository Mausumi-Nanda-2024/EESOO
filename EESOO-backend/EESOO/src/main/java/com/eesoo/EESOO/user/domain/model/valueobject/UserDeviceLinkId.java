package com.eesoo.EESOO.user.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class UserDeviceLinkId {

    private final UUID value;

    private UserDeviceLinkId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("userDeviceLinkId cannot be null");
        }
        this.value = value;
    }

    public static UserDeviceLinkId create() {
        return new UserDeviceLinkId(UUID.randomUUID());
    }

    public static UserDeviceLinkId of(UUID value) {
        return new UserDeviceLinkId(value);
    }

    public static UserDeviceLinkId fromString(String value) {
        return new UserDeviceLinkId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDeviceLinkId)) return false;
        UserDeviceLinkId that = (UserDeviceLinkId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
