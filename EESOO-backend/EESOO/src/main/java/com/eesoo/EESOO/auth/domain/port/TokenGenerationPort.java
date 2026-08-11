package com.eesoo.EESOO.auth.domain.port;

import java.time.Instant;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;
import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;

public interface TokenGenerationPort {

    TokenPair generateTokens(
            UUID userId,
            AuthSessionId authSessionId,
            Instant sessionExpiresAt
    );
}
