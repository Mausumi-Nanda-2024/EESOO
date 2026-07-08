package com.eesoo.EESOO.user.application.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("User with email " + email + " already exists. Try logging in instead.");
    }
    
}
