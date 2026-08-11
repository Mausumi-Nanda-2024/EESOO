package com.eesoo.EESOO.auth.application.logout.command;

import java.util.Objects;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;

public class LogoutCommand {

    private final UUID userId;
    private final AuthSessionId sessionId;

    public LogoutCommand(
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

    public UUID getUserId() {
        return userId;
    }

    public AuthSessionId getSessionId() {
        return sessionId;
    }
}
