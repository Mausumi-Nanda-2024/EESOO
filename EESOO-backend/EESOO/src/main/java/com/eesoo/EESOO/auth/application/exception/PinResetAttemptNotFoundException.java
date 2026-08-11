package com.eesoo.EESOO.auth.application.exception;

public class PinResetAttemptNotFoundException
        extends RuntimeException {

    public PinResetAttemptNotFoundException() {
        super(
                "PIN reset attempt was not found "
                        + "or is no longer available."
        );
    }
}
