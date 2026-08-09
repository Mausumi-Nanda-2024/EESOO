package com.eesoo.EESOO.auth.application.exception;

public class PinResetMobileVerificationException
        extends RuntimeException {

    public PinResetMobileVerificationException() {
        super(
                "Mobile number could not be verified."
        );
    }
}
