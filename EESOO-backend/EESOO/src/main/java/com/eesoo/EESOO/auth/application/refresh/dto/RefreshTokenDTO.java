package com.eesoo.EESOO.auth.application.refresh.dto;

public class RefreshTokenDTO {

    private final String refreshToken;

    public RefreshTokenDTO(
            String refreshToken
    ) {
        if (refreshToken == null
                || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Refresh token cannot be null or blank"
            );
        }

        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
