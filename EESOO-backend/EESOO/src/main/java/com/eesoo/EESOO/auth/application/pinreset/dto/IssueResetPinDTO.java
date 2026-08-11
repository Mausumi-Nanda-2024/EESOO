package com.eesoo.EESOO.auth.application.pinreset.dto;

public class IssueResetPinDTO {

    private String pinResetAttemptId;
    private String installId;

    public IssueResetPinDTO(
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
