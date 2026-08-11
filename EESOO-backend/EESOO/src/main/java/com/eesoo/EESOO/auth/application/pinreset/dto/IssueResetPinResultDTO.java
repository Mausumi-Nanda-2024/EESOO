package com.eesoo.EESOO.auth.application.pinreset.dto;

import java.time.Instant;

import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;

public class IssueResetPinResultDTO {

    private final String pinResetAttemptId;
    private final String status;
    private final String newPin;
    private final Instant pinIssuedAt;

    private IssueResetPinResultDTO(
            String pinResetAttemptId,
            String status,
            String newPin,
            Instant pinIssuedAt
    ) {
        this.pinResetAttemptId =
                pinResetAttemptId;

        this.status =
                status;

        this.newPin =
                newPin;

        this.pinIssuedAt =
                pinIssuedAt;
    }

    public static IssueResetPinResultDTO from(
            PinResetAttempt attempt,
            String newPin
    ) {
        if (attempt == null) {
            throw new IllegalArgumentException(
                    "PinResetAttempt cannot be null"
            );
        }

        if (!attempt
                .getStatus()
                .isPinIssued()) {
            throw new IllegalArgumentException(
                    "PIN-reset attempt must be PIN_ISSUED"
            );
        }

        if (newPin == null
                || newPin.isBlank()) {
            throw new IllegalArgumentException(
                    "New PIN cannot be null or blank"
            );
        }

        return new IssueResetPinResultDTO(
                attempt.getId().toString(),
                attempt.getStatus().name(),
                newPin,
                attempt.getPinIssuedAt()
        );
    }

    public String getPinResetAttemptId() {
        return pinResetAttemptId;
    }

    public String getStatus() {
        return status;
    }

    public String getNewPin() {
        return newPin;
    }

    public Instant getPinIssuedAt() {
        return pinIssuedAt;
    }
}
