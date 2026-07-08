package com.eesoo.EESOO.user.domain.model.valueobject;

import java.util.Objects;

public final class InstallId {

    private final String value;

    private InstallId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("InstallId cannot be null or blank");
        }
        this.value = value;
    }

    public static InstallId of(String value) {
        return new InstallId(value.trim());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InstallId))
            return false;
        InstallId that = (InstallId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
