package com.eesoo.EESOO.auth.domain.model.valueobject;

import java.time.Duration;
import java.time.Instant;

public class AuthSessionLifetime {

    private final Duration duration;

    private AuthSessionLifetime(
            Duration duration
    ) {
        if (duration == null) {
            throw new IllegalArgumentException(
                    "Auth session duration cannot be null"
            );
        }

        if (duration.isZero()
                || duration.isNegative()) {
            throw new IllegalArgumentException(
                    "Auth session duration must be positive"
            );
        }

        this.duration = duration;
    }

    public static AuthSessionLifetime of(
            Duration duration
    ) {
        return new AuthSessionLifetime(
                duration
        );
    }

    public Instant calculateExpiresAt(
            Instant createdAt
    ) {
        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "createdAt cannot be null"
            );
        }

        return createdAt.plus(duration);
    }

    public Duration getDuration() {
        return duration;
    }
}
