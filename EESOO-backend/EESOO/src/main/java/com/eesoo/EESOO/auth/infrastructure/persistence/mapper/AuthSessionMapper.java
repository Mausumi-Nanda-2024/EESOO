package com.eesoo.EESOO.auth.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.model.entity.AuthSession;
import com.eesoo.EESOO.auth.domain.model.enums.AuthSessionStatus;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;
import com.eesoo.EESOO.auth.domain.model.valueobject.RefreshTokenHash;
import com.eesoo.EESOO.auth.infrastructure.persistence.entity.AuthSessionJpaEntity;

@Component
public class AuthSessionMapper {

      public AuthSessionJpaEntity toEntity(
            AuthSession authSession
    ) {
        if (authSession == null) {
            throw new IllegalArgumentException(
                    "authSession cannot be null"
            );
        }

        return new AuthSessionJpaEntity(
                authSession.getId().getValue(),
                authSession.getUserId(),
                authSession.getDeviceInstallId(),
                authSession.getRefreshTokenHash().getValue(),
                authSession.getStatus().name(),
                authSession.getCreatedAt(),
                authSession.getExpiresAt(),
                authSession.getLastRotatedAt(),
                authSession.getRevokedAt(),
                authSession.getVersion()
        );
    }

    public AuthSession toDomain(
            AuthSessionJpaEntity entity
    ) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "authSessionJpaEntity cannot be null"
            );
        }

        return AuthSession.rehydrate(
                AuthSessionId.fromString(
                        entity.getId().toString()
                ),
                entity.getUserId(),
                entity.getDeviceInstallId(),
                RefreshTokenHash.of(
                        entity.getRefreshTokenHash()
                ),
                AuthSessionStatus.valueOf(
                        entity.getStatus()
                ),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getLastRotatedAt(),
                entity.getRevokedAt(),
                entity.getVersion()
        );
    }
    
}
