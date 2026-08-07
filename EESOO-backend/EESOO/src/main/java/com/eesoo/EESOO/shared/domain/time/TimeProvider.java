package com.eesoo.EESOO.shared.domain.time;

import java.time.Instant;

public interface TimeProvider {

    Instant now();
}
