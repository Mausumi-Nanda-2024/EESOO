package com.eesoo.EESOO.auth.application.login.command;

public class LoginUserCommand {

    private final String phoneNumber;
    private final String pin;
    private final String deviceId;
    private final String installId;

    public LoginUserCommand(
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

    public String getDeviceId(){
        return deviceId;
    }

    public String getInstallId(){
        return installId;
    }
    
}
