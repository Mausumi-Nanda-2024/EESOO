package com.eesoo.EESOO.user.application.register.command;

public class RegisterUserCommand {

    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String pin;
    private final String email;
    private final String installId;
    private final String deviceId;

    public RegisterUserCommand(
            String firstName,
            String lastName,
            String phoneNumber,
            String pin,
            String email,
            String installId,
            String deviceId
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.email = email;
        this.installId = installId;
        this.deviceId = deviceId;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPin() {
        return pin;
    }

    public String getEmail() {
        return email;
    }

    public String getInstallId() {
        return installId;
    }

    public String getDeviceId() {
        return deviceId;
    }

}
