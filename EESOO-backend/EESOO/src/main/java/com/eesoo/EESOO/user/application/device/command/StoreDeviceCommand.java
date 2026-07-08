package com.eesoo.EESOO.user.application.device.command;

public class StoreDeviceCommand {

    private final String deviceId;
    private final String installId;
    private final String osVersion;
    private final String platform;
    private final String deviceIdNullableReason;

    public StoreDeviceCommand(String deviceId, String installId, String osVersion, String platform, String deviceIdNullableReason) {
        this.deviceId = deviceId;
        this.installId = installId;
        this.osVersion = osVersion;
        this.platform = platform;
        this.deviceIdNullableReason = deviceIdNullableReason;

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

    public String getDeviceIdNullableReason() {
        return deviceIdNullableReason;
    }

}
