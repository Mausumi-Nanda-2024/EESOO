package com.eesoo.EESOO.user.application.exception;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
    public PhoneNumberAlreadyExistsException(String phoneNumber) {
       super("Phone number already registered. Try logging in instead.");
}
    }


