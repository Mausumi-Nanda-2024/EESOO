package com.eesoo.EESOO.auth.application.pinreset.command;

public final class ConfirmPinResetMobileCommand {

    private final String pinResetAttemptId;
    private final String phoneNumber;
    private final String installId;

    public ConfirmPinResetMobileCommand(
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
