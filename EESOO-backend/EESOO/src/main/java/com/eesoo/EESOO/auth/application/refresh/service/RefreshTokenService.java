package com.eesoo.EESOO.auth.application.refresh.service;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.auth.application.refresh.command.RefreshTokenCommand;
import com.eesoo.EESOO.auth.application.refresh.dto.RefreshTokenDTO;
import com.eesoo.EESOO.auth.application.refresh.dto.RefreshTokenResultDTO;
import com.eesoo.EESOO.auth.application.refresh.handler.RefreshTokenCommandHandler;
import com.eesoo.EESOO.auth.application.refresh.mapper.RefreshTokenMapper;

@Service
public class RefreshTokenService {

    private final RefreshTokenCommandHandler handler;

    public RefreshTokenService(
            RefreshTokenCommandHandler handler
    ) {
        this.handler = handler;
    }

    public RefreshTokenResultDTO refresh(
            RefreshTokenDTO dto
    ) {
        RefreshTokenCommand command =
                RefreshTokenMapper.toCommand(dto);

        return handler.handle(command);
    }
}
