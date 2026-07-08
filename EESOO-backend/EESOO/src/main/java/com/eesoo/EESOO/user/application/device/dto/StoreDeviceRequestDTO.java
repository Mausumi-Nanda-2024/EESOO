package com.eesoo.EESOO.user.application.device.dto;

public class StoreDeviceRequestDTO {
    
    private String deviceId;
    private String installId;
    private String osVersion;  
    private String platform;
    private String deviceIdNullableReason; 


    public StoreDeviceRequestDTO(String deviceId, String installId, String osVersion, String platform, String deviceIdNullableReason) {
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


