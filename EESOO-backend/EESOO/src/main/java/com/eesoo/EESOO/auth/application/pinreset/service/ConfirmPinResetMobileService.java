package com.eesoo.EESOO.auth.application.pinreset.service;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.auth.application.pinreset.command.ConfirmPinResetMobileCommand;
import com.eesoo.EESOO.auth.application.pinreset.dto.ConfirmPinResetMobileDTO;
import com.eesoo.EESOO.auth.application.pinreset.dto.ConfirmPinResetMobileResultDTO;
import com.eesoo.EESOO.auth.application.pinreset.handler.ConfirmPinResetMobileCommandHandler;
import com.eesoo.EESOO.auth.application.pinreset.mapper.ConfirmPinResetMobileMapper;

@Service
public class ConfirmPinResetMobileService {

    private final ConfirmPinResetMobileCommandHandler handler;

    public ConfirmPinResetMobileService(
            ConfirmPinResetMobileCommandHandler handler
    ) {
        this.handler = handler;
    }

    public ConfirmPinResetMobileResultDTO confirmMobile(
            ConfirmPinResetMobileDTO dto
    ) {
        ConfirmPinResetMobileCommand command =
                ConfirmPinResetMobileMapper.toCommand(
                        dto
                );

        return handler.handle(
                command
        );
    }
}
