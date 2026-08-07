package com.eesoo.EESOO.auth.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

public class PinResetAttemptId {

    private final UUID value;

    private PinResetAttemptId(UUID value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "pinResetAttemptId cannot be null");
        }

        this.value = value;

    }

    public static PinResetAttemptId create() {
        return new PinResetAttemptId(
                UuidCreator.getTimeOrderedEpoch());
    }

    public static PinResetAttemptId of(
            UUID value) {
        return new PinResetAttemptId(value);
    }

    public static PinResetAttemptId fromString(
            String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "pinResetAttemptId cannot be null or blank");
        }

        return new PinResetAttemptId(
                UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PinResetAttemptId)) {
            return false;
        }

        PinResetAttemptId that = (PinResetAttemptId) object;

        return Objects.equals(
                value,
                that.value);
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
