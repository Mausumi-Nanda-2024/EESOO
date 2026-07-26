package com.eesoo.EESOO.auth.domain.model.valueobject;

import java.util.Objects;

public final class RefreshTokenHash {

    private final String value;

    private RefreshTokenHash(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "refreshTokenHash cannot be null or blank"
            );
        }

        this.value = value;
    }

    public static RefreshTokenHash of(
            String value
    ) {
        return new RefreshTokenHash(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (!(object instanceof RefreshTokenHash)) {
            return false;
        }

        RefreshTokenHash that =
                (RefreshTokenHash) object;

        return Objects.equals(
                value,
                that.value
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}