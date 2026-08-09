package com.eesoo.EESOO.auth.application.logout.handler;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.auth.application.exception.AuthSessionNotUsableException;
import com.eesoo.EESOO.auth.application.logout.command.LogoutCommand;
import com.eesoo.EESOO.auth.domain.model.entity.AuthSession;
import com.eesoo.EESOO.auth.domain.repository.AuthSessionRepository;
import com.eesoo.EESOO.shared.application.cqrs.CommandHandler;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;

@Component
public class LogoutCommandHandler
        implements CommandHandler<
                LogoutCommand,
                Void
        > {

    private final AuthSessionRepository authSessionRepository;
    private final TimeProvider timeProvider;

    public LogoutCommandHandler(
            AuthSessionRepository authSessionRepository,
            TimeProvider timeProvider
    ) {
        this.authSessionRepository =
                authSessionRepository;

        this.timeProvider = timeProvider;
    }

    @Override
    @Transactional
    public Void handle(
            LogoutCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "LogoutCommand cannot be null"
            );
        }

        AuthSession authSession =
                authSessionRepository
                        .findForRevocation(
                                command.getSessionId()
                        )
                        .orElseThrow(
                                AuthSessionNotUsableException::new
                        );

        if (!authSession
                .getUserId()
                .equals(command.getUserId())) {
            throw new AuthSessionNotUsableException();
        }

        Instant revokedAt =
                timeProvider.now();

        AuthSession revokedSession =
                authSession.revoke(
                        revokedAt
                );

        authSessionRepository.save(
                revokedSession
        );

        return null;
    }
}
