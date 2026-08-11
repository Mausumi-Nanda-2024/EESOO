package com.eesoo.EESOO.auth.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.session")
public class AuthSessionProperties {

    private final Duration duration;

    public AuthSessionProperties(
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

    public Duration getDuration() {
        return duration;
    }
}
