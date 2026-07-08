package com.eesoo.EESOO.user.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class DeviceInstallId {

      private final UUID value;

    private DeviceInstallId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("deviceInstallId cannot be null");
        }
        this.value = value;
    }

    public static DeviceInstallId create() {
        return new DeviceInstallId(UUID.randomUUID());
    }

    public static DeviceInstallId of(UUID value) {
        return new DeviceInstallId(value);
    }

    public static DeviceInstallId fromString(String value) {
        return new DeviceInstallId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceInstallId)) return false;
        DeviceInstallId that = (DeviceInstallId) o;
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
