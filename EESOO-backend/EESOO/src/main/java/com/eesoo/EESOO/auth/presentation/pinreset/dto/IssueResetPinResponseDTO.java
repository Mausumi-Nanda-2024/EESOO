package com.eesoo.EESOO.auth.presentation.pinreset.dto;

import java.time.Instant;

import com.eesoo.EESOO.auth.application.pinreset.dto.IssueResetPinResultDTO;

public class IssueResetPinResponseDTO {

    private final String pinResetAttemptId;
    private final String status;
    private final String newPin;
    private final Instant pinIssuedAt;

    private IssueResetPinResponseDTO(
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

    public static IssueResetPinResponseDTO from(
            IssueResetPinResultDTO result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "IssueResetPinResultDTO cannot be null"
            );
        }

        return new IssueResetPinResponseDTO(
                result.getPinResetAttemptId(),
                result.getStatus(),
                result.getNewPin(),
                result.getPinIssuedAt()
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
