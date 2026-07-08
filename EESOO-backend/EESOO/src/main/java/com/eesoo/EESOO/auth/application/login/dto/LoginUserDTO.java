package com.eesoo.EESOO.auth.application.login.dto;

public class LoginUserDTO {
 
    private String phoneNumber;
    private String pin;
    
    public LoginUserDTO(
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
