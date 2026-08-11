package com.eesoo.EESOO.auth.application.pinreset.mapper;

import com.eesoo.EESOO.auth.application.pinreset.command.ConfirmPinResetMobileCommand;
import com.eesoo.EESOO.auth.application.pinreset.dto.ConfirmPinResetMobileDTO;
import com.eesoo.EESOO.auth.application.pinreset.dto.ConfirmPinResetMobileResultDTO;
import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;

public final class ConfirmPinResetMobileMapper {

    private ConfirmPinResetMobileMapper() {
    }

    public static ConfirmPinResetMobileCommand toCommand(
            ConfirmPinResetMobileDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "ConfirmPinResetMobileDTO cannot be null"
            );
        }

        return new ConfirmPinResetMobileCommand(
                dto.getPinResetAttemptId(),
                dto.getPhoneNumber(),
                dto.getInstallId()
        );
    }

    public static ConfirmPinResetMobileResultDTO toResult(
            PinResetAttempt attempt
    ) {
        return ConfirmPinResetMobileResultDTO.from(
                attempt
        );
    }
}
