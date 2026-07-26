package com.eesoo.EESOO.auth.application.exception;

public class InvalidCredentialsException extends RuntimeException  {

    private static final String DEFAULT_MESSAGE =
            "Invalid phone number or PIN";

    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE);
    }
    
}
