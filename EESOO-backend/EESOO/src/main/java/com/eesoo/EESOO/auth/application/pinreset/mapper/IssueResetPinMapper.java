package com.eesoo.EESOO.auth.application.pinreset.mapper;

import com.eesoo.EESOO.auth.application.pinreset.command.IssueResetPinCommand;
import com.eesoo.EESOO.auth.application.pinreset.dto.IssueResetPinDTO;
import com.eesoo.EESOO.auth.application.pinreset.dto.IssueResetPinResultDTO;
import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;

public final class IssueResetPinMapper {

    private IssueResetPinMapper() {
    }

    public static IssueResetPinCommand toCommand(
            IssueResetPinDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "IssueResetPinDTO cannot be null"
            );
        }

        return new IssueResetPinCommand(
                dto.getPinResetAttemptId(),
                dto.getInstallId()
        );
    }

    public static IssueResetPinResultDTO toResult(
            PinResetAttempt attempt,
            String newPin
    ) {
        return IssueResetPinResultDTO.from(
                attempt,
                newPin
        );
    }
}
