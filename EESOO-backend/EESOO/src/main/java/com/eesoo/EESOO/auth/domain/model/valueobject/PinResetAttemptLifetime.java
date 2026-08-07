package com.eesoo.EESOO.auth.domain.model.valueobject;

import java.time.Duration;
import java.time.Instant;

public final class PinResetAttemptLifetime {

    private final Duration firstFailureDuration;
    private final Duration resetDuration;

    private PinResetAttemptLifetime(
            Duration firstFailureDuration,
            Duration resetDuration
    ) {
        validateDuration(
                firstFailureDuration,
                "First-failure duration"
        );

        validateDuration(
                resetDuration,
                "Reset duration"
        );

        this.firstFailureDuration =
                firstFailureDuration;

        this.resetDuration =
                resetDuration;
    }

    public static PinResetAttemptLifetime of(
            Duration firstFailureDuration,
            Duration resetDuration
    ) {
        return new PinResetAttemptLifetime(
                firstFailureDuration,
                resetDuration
        );
    }

    public Instant calculateFirstFailureExpiresAt(
            Instant firstFailureAt
    ) {
        requireTime(
                firstFailureAt,
                "firstFailureAt"
        );

        return firstFailureAt.plus(
                firstFailureDuration
        );
    }

    public Instant calculateResetExpiresAt(
            Instant secondFailureAt
    ) {
        requireTime(
                secondFailureAt,
                "secondFailureAt"
        );

        return secondFailureAt.plus(
                resetDuration
        );
    }

    public Duration getFirstFailureDuration() {
        return firstFailureDuration;
    }

    public Duration getResetDuration() {
        return resetDuration;
    }

    private static void validateDuration(
            Duration duration,
            String fieldName
    ) {
        if (duration == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }

        if (duration.isZero()
                || duration.isNegative()) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }
    }

    private static void requireTime(
            Instant value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }
    }
}
