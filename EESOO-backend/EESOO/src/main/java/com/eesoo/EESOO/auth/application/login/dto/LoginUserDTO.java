package com.eesoo.EESOO.auth.application.login.dto;

public class LoginUserDTO {
 
    private String phoneNumber;
    private String pin;
    private String deviceId;
    private String installId;
    
    public LoginUserDTO(
            String phoneNumber,
            String pin,
            String deviceId,
            String installId
    ) {
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.deviceId = deviceId;
        this.installId = installId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPin() {
        return pin;
    }
    
    public String getDeviceId() {
        return deviceId;
    }

    public String getInstallId() {
        return installId;
    }
}
