package com.eesoo.EESOO.auth.application.logout.service;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.auth.application.logout.command.LogoutCommand;
import com.eesoo.EESOO.auth.application.logout.dto.LogoutDTO;
import com.eesoo.EESOO.auth.application.logout.handler.LogoutCommandHandler;
import com.eesoo.EESOO.auth.application.logout.mapper.LogoutMapper;

@Service
public class LogoutService {

    private final LogoutCommandHandler handler;

    public LogoutService(
            LogoutCommandHandler handler
    ) {
        this.handler = handler;
    }

    public void logout(
            LogoutDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "LogoutDTO cannot be null"
            );
        }

        LogoutCommand command =
                LogoutMapper.toCommand(
                        dto
                );

        handler.handle(command);
    }
}
