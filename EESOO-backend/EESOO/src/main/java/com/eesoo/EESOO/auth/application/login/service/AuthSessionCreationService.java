package com.eesoo.EESOO.auth.application.login.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.auth.domain.model.entity.AuthSession;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionLifetime;
import com.eesoo.EESOO.auth.domain.model.valueobject.RefreshTokenHash;
import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;
import com.eesoo.EESOO.auth.domain.port.RefreshTokenHasherPort;
import com.eesoo.EESOO.auth.domain.port.TokenGenerationPort;
import com.eesoo.EESOO.auth.domain.repository.AuthSessionRepository;

@Service
public class AuthSessionCreationService {

        private final AuthSessionRepository authSessionRepository;
        private final TokenGenerationPort tokenGenerationPort;
        private final RefreshTokenHasherPort refreshTokenHasherPort;
        private final AuthSessionLifetime authSessionLifetime;
        private final Clock clock;

        public AuthSessionCreationService(
                        AuthSessionRepository authSessionRepository,
                        TokenGenerationPort tokenGenerationPort,
                        RefreshTokenHasherPort refreshTokenHasherPort,
                        AuthSessionLifetime authSessionLifetime,
                        Clock clock) {
                this.authSessionRepository = authSessionRepository;

                this.tokenGenerationPort = tokenGenerationPort;

                this.refreshTokenHasherPort = refreshTokenHasherPort;

                this.authSessionLifetime = authSessionLifetime;

                this.clock = clock;
        }

        @Transactional
        public TokenPair createSession(UUID userId, UUID deviceInstallId) {
                if (userId == null) {
                        throw new IllegalArgumentException(
                                        "userId cannot be null");
                }

                if (deviceInstallId == null) {
                        throw new IllegalArgumentException(
                                        "deviceInstallId cannot be null");
                }

                Instant createdAt = clock.instant();

                Instant sessionExpiresAt = authSessionLifetime.calculateExpiresAt(createdAt);

                AuthSessionId authSessionId = AuthSessionId.create();

                TokenPair tokenPair = tokenGenerationPort.generateTokens(
                                userId,
                                authSessionId,
                                sessionExpiresAt);

                RefreshTokenHash refreshTokenHash = refreshTokenHasherPort.hash(
                                tokenPair.getRefreshToken());

                AuthSession authSession = AuthSession.createActive(
                                authSessionId,
                                userId,
                                deviceInstallId,
                                refreshTokenHash,
                                createdAt,
                                sessionExpiresAt);

                authSessionRepository.save(
                                authSession);

                return tokenPair;
        }

}
