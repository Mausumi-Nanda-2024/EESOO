package com.eesoo.EESOO.auth.application.pinreset.dto;

import java.time.Instant;

import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;

public class ConfirmPinResetMobileResultDTO {

    private final String pinResetAttemptId;
    private final String status;
    private final Instant mobileConfirmedAt;

    private ConfirmPinResetMobileResultDTO(
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

    public static ConfirmPinResetMobileResultDTO from(
            PinResetAttempt attempt
    ) {
        if (attempt == null) {
            throw new IllegalArgumentException(
                    "PinResetAttempt cannot be null"
            );
        }

        if (!attempt
                .getStatus()
                .isMobileConfirmed()) {
            throw new IllegalArgumentException(
                    "PIN-reset attempt must be MOBILE_CONFIRMED"
            );
        }

        return new ConfirmPinResetMobileResultDTO(
                attempt.getId().toString(),
                attempt.getStatus().name(),
                attempt.getMobileConfirmedAt()
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
