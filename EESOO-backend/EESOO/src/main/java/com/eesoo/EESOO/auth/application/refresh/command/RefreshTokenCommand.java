package com.eesoo.EESOO.auth.application.refresh.command;

public class RefreshTokenCommand {

    private final String rawRefreshToken;

    public RefreshTokenCommand(
            String rawRefreshToken
    ) {
        if (rawRefreshToken == null
                || rawRefreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Refresh token cannot be null or blank"
            );
        }

        this.rawRefreshToken = rawRefreshToken;
    }

    public String getRawRefreshToken() {
        return rawRefreshToken;
    }
}
