package com.eesoo.EESOO.auth.domain.exception;

public class InvalidTokenException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Token is invalid or expired";

    public InvalidTokenException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidTokenException(
            Throwable cause
    ) {
        super(
                DEFAULT_MESSAGE,
                cause
        );
    }
}
