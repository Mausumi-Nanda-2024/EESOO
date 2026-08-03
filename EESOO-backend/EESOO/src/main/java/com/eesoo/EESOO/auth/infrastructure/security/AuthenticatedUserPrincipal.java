package com.eesoo.EESOO.auth.infrastructure.security;

import java.security.Principal;
import java.util.Objects;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;

public class AuthenticatedUserPrincipal
        implements Principal {

    private final UUID userId;
    private final AuthSessionId sessionId;

    public AuthenticatedUserPrincipal(
            UUID userId,
            AuthSessionId sessionId
    ) {
        this.userId = Objects.requireNonNull(
                userId,
                "userId cannot be null"
        );

        this.sessionId = Objects.requireNonNull(
                sessionId,
                "sessionId cannot be null"
        );
    }

    @Override
    public String getName() {
        return userId.toString();
    }

    public UUID getUserId() {
        return userId;
    }

    public AuthSessionId getSessionId() {
        return sessionId;
    }
}
