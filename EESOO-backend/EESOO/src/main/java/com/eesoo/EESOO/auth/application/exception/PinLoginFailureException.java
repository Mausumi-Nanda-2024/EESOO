package com.eesoo.EESOO.auth.application.exception;

import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.enums.PinResetStatus;
import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptId;

public class PinLoginFailureException
        extends RuntimeException {

    private final PinResetAttemptId attemptId;
    private final PinResetStatus resetStatus;
    private final String code;
    private final int remainingAttempts;

    public PinLoginFailureException(
            PinResetAttempt attempt
    ) {
        super(
                messageFor(
                        requireAttempt(attempt)
                                .getStatus()
                )
        );

        this.attemptId =
                attempt.getId();

        this.resetStatus =
                attempt.getStatus();

        this.code =
                codeFor(
                        attempt.getStatus()
                );

        this.remainingAttempts =
                attempt.getRemainingAttempts();
    }

    private static PinResetAttempt requireAttempt(
            PinResetAttempt attempt
    ) {
        if (attempt == null) {
            throw new IllegalArgumentException(
                    "PinResetAttempt cannot be null"
            );
        }

        return attempt;
    }

    private static String messageFor(
            PinResetStatus status
    ) {
        return switch (status) {
            case FIRST_FAILURE ->
                    "Incorrect PIN. One attempt remaining.";

            case RESET_AVAILABLE ->
                    "PIN reset is available.";

            case MOBILE_CONFIRMED ->
                    "PIN reset is already in progress.";

            case PIN_ISSUED ->
                    "A replacement PIN has already been issued. "
                            + "Log in using the new PIN.";
        };
    }

    private static String codeFor(
            PinResetStatus status
    ) {
        return switch (status) {
            case FIRST_FAILURE ->
                    "PIN_INCORRECT";

            case RESET_AVAILABLE ->
                    "PIN_RESET_AVAILABLE";

            case MOBILE_CONFIRMED ->
                    "PIN_RESET_IN_PROGRESS";

            case PIN_ISSUED ->
                    "PIN_ALREADY_ISSUED";
        };
    }

    public PinResetAttemptId getAttemptId() {
        return attemptId;
    }

    public PinResetStatus getResetStatus() {
        return resetStatus;
    }

    public String getCode() {
        return code;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
