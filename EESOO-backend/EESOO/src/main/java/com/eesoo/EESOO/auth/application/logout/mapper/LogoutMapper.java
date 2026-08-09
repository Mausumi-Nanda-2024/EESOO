package com.eesoo.EESOO.auth.application.logout.mapper;

import com.eesoo.EESOO.auth.application.logout.command.LogoutCommand;
import com.eesoo.EESOO.auth.application.logout.dto.LogoutDTO;

public final class LogoutMapper {

    private LogoutMapper() {
    }

    public static LogoutCommand toCommand(
            LogoutDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "LogoutDTO cannot be null"
            );
        }

        return new LogoutCommand(
                dto.getUserId(),
                dto.getSessionId()
        );
    }
}
