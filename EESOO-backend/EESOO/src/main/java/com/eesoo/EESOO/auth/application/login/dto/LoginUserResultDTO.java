package com.eesoo.EESOO.auth.application.login.dto;

import com.eesoo.EESOO.auth.domain.model.entity.AuthUser;
import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;

public class LoginUserResultDTO {

    private final String userId;
    private final String username;
    private final String accessToken;
    private final String refreshToken;
    private final boolean deviceLinked;
    private final String deviceLinkFailureReason;

    private LoginUserResultDTO(
        String userId,
        String username,
        String accessToken,
        String refreshToken,
        boolean deviceLinked,
        String deviceLinkFailureReason
    ){
        this.userId = userId;
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.deviceLinked = deviceLinked;
        this.deviceLinkFailureReason = deviceLinkFailureReason;
    }

    public static LoginUserResultDTO from(
            AuthUser authUser,
            TokenPair tokenPair,
            boolean deviceLinked,
            String deviceLinkFailureReason
    ) {
        return new LoginUserResultDTO(
                authUser.getUserId().toString(),
                authUser.getUsername(),
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                deviceLinked,
                deviceLinkFailureReason
        );
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean isDeviceLinked() {
        return deviceLinked;
    }

    public String getDeviceLinkFailureReason() {
        return deviceLinkFailureReason;
    }

    
}
