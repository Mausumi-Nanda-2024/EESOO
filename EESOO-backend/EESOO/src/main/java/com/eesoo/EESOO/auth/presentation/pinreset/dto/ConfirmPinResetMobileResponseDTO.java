package com.eesoo.EESOO.auth.presentation.pinreset.dto;

import java.time.Instant;

import com.eesoo.EESOO.auth.application.pinreset.dto.ConfirmPinResetMobileResultDTO;

public class ConfirmPinResetMobileResponseDTO {

    private final String pinResetAttemptId;
    private final String status;
    private final Instant mobileConfirmedAt;

    private ConfirmPinResetMobileResponseDTO(
            String pinResetAttemptId,
            String status,
            Instant mobileConfirmedAt
    ) {
        this.pinResetAttemptId =
                pinResetAttemptId;

        this.status =
                status;

        this.mobileConfirmedAt =
                mobileConfirmedAt;
    }

    public static ConfirmPinResetMobileResponseDTO from(
            ConfirmPinResetMobileResultDTO result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "ConfirmPinResetMobileResultDTO cannot be null"
            );
        }

        return new ConfirmPinResetMobileResponseDTO(
                result.getPinResetAttemptId(),
                result.getStatus(),
                result.getMobileConfirmedAt()
        );
    }

    public String getPinResetAttemptId() {
        return pinResetAttemptId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getMobileConfirmedAt() {
        return mobileConfirmedAt;
    }
}
