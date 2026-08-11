package com.eesoo.EESOO.auth.application.pinreset.dto;

public class ConfirmPinResetMobileDTO {

    private String pinResetAttemptId;
    private String phoneNumber;
    private String installId;

    public ConfirmPinResetMobileDTO(
            String pinResetAttemptId,
            String phoneNumber,
            String installId
    ) {
        this.pinResetAttemptId =
                pinResetAttemptId;

        this.phoneNumber =
                phoneNumber;

        this.installId =
                installId;
    }

    public String getPinResetAttemptId() {
        return pinResetAttemptId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getInstallId() {
        return installId;
    }
}
