package com.eesoo.EESOO.auth.presentation.login.dto;

import com.eesoo.EESOO.auth.application.login.dto.LoginUserResultDTO;

public class LoginUserResponseDTO {

    private final String userId;
    private final String username;
    private final String accessToken;
    private final String refreshToken;
    private final boolean deviceLinked;
    private final String deviceLinkFailureReason;

    private LoginUserResponseDTO(
            String userId,
            String username,
            String accessToken,
            String refreshToken,
            boolean deviceLinked,
            String deviceLinkFailureReason) {
        this.userId = userId;
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.deviceLinked = deviceLinked;
        this.deviceLinkFailureReason = deviceLinkFailureReason;
    }

    public static LoginUserResponseDTO from(
            LoginUserResultDTO result) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "LoginUserResultDTO cannot be null");
        }

        return new LoginUserResponseDTO(
                result.getUserId(),
                result.getUsername(),
                result.getAccessToken(),
                result.getRefreshToken(),
                result.isDeviceLinked(),
                result.getDeviceLinkFailureReason());
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

    @Override
    public String toString() {
        return "LoginUserResponseDTO{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", accessToken='[PROTECTED]'" +
                ", refreshToken='[PROTECTED]'" +
                ", deviceLinked=" + deviceLinked +
                ", deviceLinkFailureReason='" +
                deviceLinkFailureReason + '\'' +
                '}';
    }
}
