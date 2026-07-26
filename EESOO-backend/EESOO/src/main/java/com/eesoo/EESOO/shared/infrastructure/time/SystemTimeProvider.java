package com.eesoo.EESOO.shared.infrastructure.time;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.shared.domain.time.TimeProvider;

@Component
public final class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
