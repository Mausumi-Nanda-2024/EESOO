package com.eesoo.EESOO.auth.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptId;
import com.eesoo.EESOO.auth.domain.repository.PinResetAttemptRepository;
import com.eesoo.EESOO.auth.infrastructure.persistence.entity.PinResetAttemptJpaEntity;
import com.eesoo.EESOO.auth.infrastructure.persistence.mapper.PinResetAttemptMapper;

@Repository
public class JpaPinResetAttemptRepository
        implements PinResetAttemptRepository {

    private final SpringDataPinResetAttemptRepository springRepository;
    private final PinResetAttemptMapper mapper;

    public JpaPinResetAttemptRepository(
            SpringDataPinResetAttemptRepository springRepository,
            PinResetAttemptMapper mapper
    ) {
        this.springRepository = springRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean createFirstFailureIfAbsent(
            PinResetAttempt attempt
    ) {
        validateFirstFailureAttempt(attempt);

        int insertedRows =
                springRepository.insertFirstFailureIfAbsent(
                        attempt.getId().getValue(),
                        attempt.getUserId(),
                        attempt.getDeviceInstallId(),
                        attempt.getCreatedAt(),
                        attempt.getExpiresAt()
                );

        if (insertedRows == 1) {
            return true;
        }

        if (insertedRows == 0) {
            return false;
        }

        throw new IllegalStateException(
                "Unexpected number of inserted PIN-reset rows: "
                        + insertedRows
        );
    }

    @Override
    public PinResetAttempt save(
            PinResetAttempt attempt
    ) {
        if (attempt == null) {
            throw new IllegalArgumentException(
                    "PinResetAttempt cannot be null"
            );
        }

        PinResetAttemptJpaEntity entity =
                mapper.toEntity(attempt);

        PinResetAttemptJpaEntity savedEntity =
                springRepository.saveAndFlush(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PinResetAttempt> findById(
            PinResetAttemptId attemptId
    ) {
        requireAttemptId(attemptId);

        return springRepository
                .findById(attemptId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<PinResetAttempt> findForUpdateById(
            PinResetAttemptId attemptId
    ) {
        requireAttemptId(attemptId);

        return springRepository
                .findForUpdateById(
                        attemptId.getValue()
                )
                .map(mapper::toDomain);
    }

    @Override
    public Optional<PinResetAttempt>
            findForUpdateByUserIdAndDeviceInstallId(
                    UUID userId,
                    UUID deviceInstallId
            ) {
        requireUserAndDevice(
                userId,
                deviceInstallId
        );

        return springRepository
                .findForUpdateByUserIdAndDeviceInstallId(
                        userId,
                        deviceInstallId
                )
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(
            PinResetAttemptId attemptId
    ) {
        requireAttemptId(attemptId);

        springRepository.deleteById(
                attemptId.getValue()
        );
    }

    @Override
    public void deleteByUserIdAndDeviceInstallId(
            UUID userId,
            UUID deviceInstallId
    ) {
        requireUserAndDevice(
                userId,
                deviceInstallId
        );

        springRepository
                .deleteByUserIdAndDeviceInstallId(
                        userId,
                        deviceInstallId
                );
    }

    private static void validateFirstFailureAttempt(
            PinResetAttempt attempt
    ) {
        if (attempt == null) {
            throw new IllegalArgumentException(
                    "PinResetAttempt cannot be null"
            );
        }

        if (!attempt.getStatus().isFirstFailure()) {
            throw new IllegalArgumentException(
                    "Only a FIRST_FAILURE attempt can be created"
            );
        }

        if (attempt.getFailedAttempts() != 1) {
            throw new IllegalArgumentException(
                    "A new PIN-reset attempt must have one failure"
            );
        }

        if (attempt.getVersion() != null) {
            throw new IllegalArgumentException(
                    "A new PIN-reset attempt cannot already have a version"
            );
        }
    }

    private static void requireAttemptId(
            PinResetAttemptId attemptId
    ) {
        if (attemptId == null) {
            throw new IllegalArgumentException(
                    "PinResetAttemptId cannot be null"
            );
        }
    }

    private static void requireUserAndDevice(
            UUID userId,
            UUID deviceInstallId
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId cannot be null"
            );
        }

        if (deviceInstallId == null) {
            throw new IllegalArgumentException(
                    "deviceInstallId cannot be null"
            );
        }
    }
}