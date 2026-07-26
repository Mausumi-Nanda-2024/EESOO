package com.eesoo.EESOO.auth.application.exception;

public class AuthSessionNotUsableException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Authentication session is no longer active";

    public AuthSessionNotUsableException() {
        super(DEFAULT_MESSAGE);
    }
}
