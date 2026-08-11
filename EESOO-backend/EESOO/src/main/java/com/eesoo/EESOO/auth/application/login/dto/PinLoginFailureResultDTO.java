package com.eesoo.EESOO.auth.application.login.dto;

import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.enums.PinResetStatus;

public final class PinLoginFailureResultDTO {

    private final String pinResetAttemptId;
    private final PinResetStatus resetStatus;
    private final String code;
    private final String message;
    private final int remainingAttempts;

    private PinLoginFailureResultDTO(
            String pinResetAttemptId,
            PinResetStatus resetStatus,
            String code,
            String message,
            int remainingAttempts
    ) {
        this.pinResetAttemptId =
                pinResetAttemptId;

        this.resetStatus =
                resetStatus;

        this.code =
                code;

        this.message =
                message;

        this.remainingAttempts =
                remainingAttempts;
    }

    public static PinLoginFailureResultDTO from(
            PinResetAttempt attempt
    ) {
        if (attempt == null) {
            throw new IllegalArgumentException(
                    "PinResetAttempt cannot be null"
            );
        }

        PinResetStatus status =
                attempt.getStatus();

        return new PinLoginFailureResultDTO(
                status.isFirstFailure()
                        ? null
                        : attempt.getId().toString(),
                status,
                codeFor(status),
                messageFor(status),
                attempt.getRemainingAttempts()
        );
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

    public String getPinResetAttemptId() {
        return pinResetAttemptId;
    }

    public PinResetStatus getResetStatus() {
        return resetStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
