package com.eesoo.EESOO.auth.application.refresh.handler;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.auth.application.exception.AuthSessionNotUsableException;
import com.eesoo.EESOO.auth.application.exception.RefreshTokenReplayDetectedException;
import com.eesoo.EESOO.auth.application.refresh.command.RefreshTokenCommand;
import com.eesoo.EESOO.auth.application.refresh.dto.RefreshTokenResultDTO;
import com.eesoo.EESOO.auth.domain.exception.InvalidTokenException;
import com.eesoo.EESOO.auth.domain.model.entity.AuthSession;
import com.eesoo.EESOO.auth.domain.model.token.RefreshTokenClaims;
import com.eesoo.EESOO.auth.domain.model.valueobject.RefreshTokenHash;
import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;
import com.eesoo.EESOO.auth.domain.port.RefreshTokenHasherPort;
import com.eesoo.EESOO.auth.domain.port.TokenGenerationPort;
import com.eesoo.EESOO.auth.domain.port.TokenValidationPort;
import com.eesoo.EESOO.auth.domain.repository.AuthSessionRepository;
import com.eesoo.EESOO.shared.application.cqrs.CommandHandler;

@Component
public class RefreshTokenCommandHandler
        implements CommandHandler<
                RefreshTokenCommand,
                RefreshTokenResultDTO
        > {

    private final TokenValidationPort tokenValidationPort;
    private final AuthSessionRepository authSessionRepository;
    private final RefreshTokenHasherPort refreshTokenHasherPort;
    private final TokenGenerationPort tokenGenerationPort;
    private final Clock clock;

    public RefreshTokenCommandHandler(
            TokenValidationPort tokenValidationPort,
            AuthSessionRepository authSessionRepository,
            RefreshTokenHasherPort refreshTokenHasherPort,
            TokenGenerationPort tokenGenerationPort,
            Clock clock
    ) {
        this.tokenValidationPort = tokenValidationPort;
        this.authSessionRepository = authSessionRepository;
        this.refreshTokenHasherPort = refreshTokenHasherPort;
        this.tokenGenerationPort = tokenGenerationPort;
        this.clock = clock;
    }

    @Override
    @Transactional(
            noRollbackFor =
                    RefreshTokenReplayDetectedException.class
    )
    public RefreshTokenResultDTO handle(
            RefreshTokenCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "RefreshTokenCommand cannot be null"
            );
        }

        String rawRefreshToken =
                command.getRawRefreshToken();

        RefreshTokenClaims claims =
                tokenValidationPort
                        .validateRefreshToken(
                                rawRefreshToken
                        );

        AuthSession authSession =
                authSessionRepository
                        .findForRefreshRotation(
                                claims.getSessionId()
                        )
                        .orElseThrow(
                                AuthSessionNotUsableException::new
                        );

        Instant rotatedAt =
                clock.instant();

        validateSession(
                authSession,
                claims,
                rotatedAt
        );

        revokeSessionWhenReplayDetected(
                authSession,
                rawRefreshToken,
                rotatedAt
        );

        TokenPair newTokenPair =
                tokenGenerationPort.generateTokens(
                        authSession.getUserId(),
                        authSession.getId(),
                        authSession.getExpiresAt()
                );

        RefreshTokenHash newRefreshTokenHash =
                refreshTokenHasherPort.hash(
                        newTokenPair.getRefreshToken()
                );

        AuthSession rotatedSession =
                authSession.rotateRefreshToken(
                        newRefreshTokenHash,
                        rotatedAt
                );

        authSessionRepository.save(
                rotatedSession
        );

        return RefreshTokenResultDTO.from(
                newTokenPair
        );
    }

    private void validateSession(
            AuthSession authSession,
            RefreshTokenClaims claims,
            Instant currentTime
    ) {
        if (!authSession
                .getUserId()
                .equals(claims.getUserId())) {
            throw new InvalidTokenException();
        }

        if (!authSession.isUsable(currentTime)) {
            throw new AuthSessionNotUsableException();
        }
    }

    private void revokeSessionWhenReplayDetected(
            AuthSession authSession,
            String rawRefreshToken,
            Instant detectedAt
    ) {
        boolean refreshTokenMatches =
                refreshTokenHasherPort.matches(
                        rawRefreshToken,
                        authSession.getRefreshTokenHash()
                );

        if (!refreshTokenMatches) {
            AuthSession revokedSession =
                    authSession.revoke(
                            detectedAt
                    );

            authSessionRepository.save(
                    revokedSession
            );

            throw new RefreshTokenReplayDetectedException();
        }
    }
}
