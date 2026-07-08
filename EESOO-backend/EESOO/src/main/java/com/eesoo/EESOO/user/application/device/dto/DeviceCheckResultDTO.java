package com.eesoo.EESOO.user.application.device.dto;

import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceLinkStatus;
import com.eesoo.EESOO.user.domain.model.entity.User;
import com.eesoo.EESOO.user.domain.model.entity.UserDeviceLink;

public class DeviceCheckResultDTO {

    private final DeviceLinkStatus status;
    private final String failureReason;
    private final String userId;
    private final String username;
    private final String phoneNumber;

    private DeviceCheckResultDTO(
            DeviceLinkStatus status,
            String failureReason,
            String userId,
            String username,
            String phoneNumber
    ) {

        this.status = status;
        this.failureReason = failureReason;
        this.userId = userId;
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    public static DeviceCheckResultDTO linked(
           UserDeviceLink link,
           User user
    ) {
        return new DeviceCheckResultDTO(
                DeviceLinkStatus.LINKED,
                null,
                link.getUserId().getValue().toString(),
                user.getUsername().getValue(),
                user.getPhoneNumber().getValue()
        );
    }

    public static DeviceCheckResultDTO notLinked(String failureReason) {
        return new DeviceCheckResultDTO(
                DeviceLinkStatus.NOT_LINKED,
                failureReason,
                null,
                null,
                null
        );
    }

    public DeviceLinkStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
