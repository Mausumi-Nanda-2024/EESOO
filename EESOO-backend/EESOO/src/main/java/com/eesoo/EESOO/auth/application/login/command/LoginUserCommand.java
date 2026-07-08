package com.eesoo.EESOO.auth.application.login.command;

public class LoginUserCommand {

    private final String phoneNumber;
    private final String pin;

    public LoginUserCommand(
            String phoneNumber,
            String pin
    ) {
        this.phoneNumber = phoneNumber;
        this.pin = pin;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPin() {
        return pin;
    }
    
}
