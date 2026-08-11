package com.eesoo.EESOO.auth.application.pinreset.command;

public final class IssueResetPinCommand {

    private final String pinResetAttemptId;
    private final String installId;

    public IssueResetPinCommand(
            String pinResetAttemptId,
            String installId
    ) {
        this.pinResetAttemptId =
                pinResetAttemptId;

        this.installId =
                installId;
    }

    public String getPinResetAttemptId() {
        return pinResetAttemptId;
    }

    public String getInstallId() {
        return installId;
    }
}
