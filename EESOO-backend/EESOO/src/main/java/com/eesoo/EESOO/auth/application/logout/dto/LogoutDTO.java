package com.eesoo.EESOO.auth.application.logout.dto;

import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;

public class LogoutDTO {

    private UUID userId;
    private AuthSessionId sessionId;

    public LogoutDTO(
            UUID userId,
            AuthSessionId sessionId
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public AuthSessionId getSessionId() {
        return sessionId;
    }
}
