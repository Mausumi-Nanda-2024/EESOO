package com.eesoo.EESOO.auth.application.logout.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.auth.application.logout.command.LogoutCommand;
import com.eesoo.EESOO.auth.application.logout.handler.LogoutCommandHandler;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;

@Service
public class LogoutService {

    private final LogoutCommandHandler handler;

    public LogoutService(
            LogoutCommandHandler handler
    ) {
        this.handler = handler;
    }

    public void logout(
            UUID userId,
            AuthSessionId sessionId
    ) {
        LogoutCommand command =
                new LogoutCommand(
                        userId,
                        sessionId
                );

        handler.handle(command);
    }
}
