package com.eesoo.EESOO.auth.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.enums.PinResetStatus;
import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptId;
import com.eesoo.EESOO.auth.infrastructure.persistence.entity.PinResetAttemptJpaEntity;

@Component
public class PinResetAttemptMapper {

     public PinResetAttemptJpaEntity toEntity(
            PinResetAttempt attempt
    ) {
        if (attempt == null) {
            throw new IllegalArgumentException(
                    "PinResetAttempt cannot be null"
            );
        }

        return new PinResetAttemptJpaEntity(
                attempt.getId().getValue(),
                attempt.getUserId(),
                attempt.getDeviceInstallId(),
                attempt.getFailedAttempts(),
                attempt.getStatus().name(),
                attempt.getCreatedAt(),
                attempt.getExpiresAt(),
                attempt.getMobileConfirmedAt(),
                attempt.getPinIssuedAt(),
                attempt.getVersion()
        );
    }

    public PinResetAttempt toDomain(
            PinResetAttemptJpaEntity entity
    ) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "PinResetAttemptJpaEntity cannot be null"
            );
        }

        return PinResetAttempt.rehydrate(
                PinResetAttemptId.of(
                        entity.getId()
                ),
                entity.getUserId(),
                entity.getDeviceInstallId(),
                entity.getFailedAttempts(),
                PinResetStatus.valueOf(
                        entity.getStatus()
                ),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getMobileConfirmedAt(),
                entity.getPinIssuedAt(),
                entity.getVersion()
        );
    }
    
}
