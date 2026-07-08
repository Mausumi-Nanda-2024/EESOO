package com.eesoo.EESOO.user.application.exception;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
    public PhoneNumberAlreadyExistsException(String phoneNumber) {
        super("User with phone number " + phoneNumber + " already exists. Try logging in instead.");
    }

}
