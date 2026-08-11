package com.eesoo.EESOO.shared.infrastructure.time;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.shared.domain.time.TimeProvider;

@Component
public final class SystemTimeProvider implements TimeProvider {

    private final Clock clock;

    public SystemTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant now() {
        return clock.instant();
    }
}
