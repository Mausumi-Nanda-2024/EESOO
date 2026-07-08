package com.eesoo.EESOO.user.application.device.dto;

import java.time.LocalDateTime;

import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceRegistrationResult;
import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;

public class StoreDeviceResultDTO {

    private final String deviceId;
    private final String installId;
    private final String osVersion;
    private final String platform;
    private final LocalDateTime registrationTimestamp;
    private final DeviceRegistrationResult operationResult;
    private final String deviceIdNullableReason;

    private StoreDeviceResultDTO(
            String deviceId,
            String installId,
            String osVersion,
            String platform,
            LocalDateTime registrationTimestamp,
            DeviceRegistrationResult operationResult,
            String deviceIdNullableReason
    ) {
        this.deviceId = deviceId;
        this.installId = installId;
        this.osVersion = osVersion;
        this.platform = platform;
        this.registrationTimestamp = registrationTimestamp;
        this.operationResult = operationResult;
        this.deviceIdNullableReason = deviceIdNullableReason;
    }

    public static StoreDeviceResultDTO from(DeviceInstall device, DeviceRegistrationResult operationResult) {
        return new StoreDeviceResultDTO(
                device.getDeviceId() != null ? device.getDeviceId().getValue().toString() : null,
                device.getInstallId().getValue().toString(),
                device.getOsVersion(),
                device.getPlatform(),
                device.getFirstSeenAt(), // Using firstSeenAt as registrationTimestamp
                operationResult,
                device.getDeviceIdNullableReason() != null
                        ? device.getDeviceIdNullableReason().name()
                        : null
        );
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getInstallId() {
        return installId;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public LocalDateTime getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public DeviceRegistrationResult getOperationResult() {
        return operationResult;
    }

    public String getDeviceIdNullableReason() {
        return deviceIdNullableReason;
    }
}
