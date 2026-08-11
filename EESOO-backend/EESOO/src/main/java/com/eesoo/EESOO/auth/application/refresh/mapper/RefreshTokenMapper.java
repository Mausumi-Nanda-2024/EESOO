package com.eesoo.EESOO.auth.application.refresh.mapper;

import com.eesoo.EESOO.auth.application.refresh.command.RefreshTokenCommand;
import com.eesoo.EESOO.auth.application.refresh.dto.RefreshTokenDTO;

public final class RefreshTokenMapper {

    private RefreshTokenMapper() {
    }

    public static RefreshTokenCommand toCommand(
            RefreshTokenDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "RefreshTokenDTO cannot be null"
            );
        }

        return new RefreshTokenCommand(
                dto.getRefreshToken()
        );
    }
}
