package com.eesoo.EESOO.auth.presentation.refresh.dto;

import com.eesoo.EESOO.auth.application.refresh.dto.RefreshTokenResultDTO;

public class RefreshTokenResponseDTO {

    private final String accessToken;
    private final String refreshToken;

    private RefreshTokenResponseDTO(
            String accessToken,
            String refreshToken
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static RefreshTokenResponseDTO from(
            RefreshTokenResultDTO result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "RefreshTokenResultDTO cannot be null"
            );
        }

        return new RefreshTokenResponseDTO(
                result.getAccessToken(),
                result.getRefreshToken()
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
        return "RefreshTokenResponseDTO{" +
                "accessToken='[PROTECTED]'" +
                ", refreshToken='[PROTECTED]'" +
                '}';
    }
}
