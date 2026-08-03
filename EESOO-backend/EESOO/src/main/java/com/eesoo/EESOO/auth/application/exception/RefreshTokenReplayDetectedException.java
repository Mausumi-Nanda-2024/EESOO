package com.eesoo.EESOO.auth.application.exception;

public class RefreshTokenReplayDetectedException
        extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Refresh token reuse detected; authentication session has been revoked";

    public RefreshTokenReplayDetectedException() {
        super(DEFAULT_MESSAGE);
    }
}
