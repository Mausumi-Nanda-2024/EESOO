package com.eesoo.EESOO.auth.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.pin-reset")
public class PinResetProperties {

    private final Duration firstFailureDuration;
    private final Duration resetDuration;

    public PinResetProperties(
            Duration firstFailureDuration,
            Duration resetDuration
    ) {
        validateDuration(
                firstFailureDuration,
                "PIN-reset first-failure duration"
        );

        validateDuration(
                resetDuration,
                "PIN-reset duration"
        );

        this.firstFailureDuration =
                firstFailureDuration;

        this.resetDuration =
                resetDuration;
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
}
