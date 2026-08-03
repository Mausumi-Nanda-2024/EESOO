package com.eesoo.EESOO.auth.application.refresh.dto;

import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;

public class RefreshTokenResultDTO {

    private final String accessToken;
    private final String refreshToken;

    private RefreshTokenResultDTO(
            String accessToken,
            String refreshToken
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static RefreshTokenResultDTO from(
            TokenPair tokenPair
    ) {
        if (tokenPair == null) {
            throw new IllegalArgumentException(
                    "TokenPair cannot be null"
            );
        }

        return new RefreshTokenResultDTO(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken()
        );
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String toString() {
        return "RefreshTokenResultDTO{" +
                "accessToken='[PROTECTED]'" +
                ", refreshToken='[PROTECTED]'" +
                '}';
    }
}
