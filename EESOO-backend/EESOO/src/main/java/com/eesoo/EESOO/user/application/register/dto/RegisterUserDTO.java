package com.eesoo.EESOO.user.application.register.dto;

public class RegisterUserDTO {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String pin;
    private String email;
    private String installId;
    private String deviceId;

    public RegisterUserDTO(
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
