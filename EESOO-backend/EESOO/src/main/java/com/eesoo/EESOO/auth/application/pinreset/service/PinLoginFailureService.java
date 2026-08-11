package com.eesoo.EESOO.auth.application.pinreset.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptLifetime;
import com.eesoo.EESOO.auth.domain.repository.PinResetAttemptRepository;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;

@Service
public class PinLoginFailureService {

    private final PinResetAttemptRepository repository;
    private final PinResetAttemptLifetime lifetime;
    private final TimeProvider timeProvider;

    public PinLoginFailureService(
            PinResetAttemptRepository repository,
            PinResetAttemptLifetime lifetime,
            TimeProvider timeProvider) {
        this.repository = repository;
        this.lifetime = lifetime;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public PinResetAttempt recordFailedPin(
            UUID userId,
            UUID deviceInstallId) {
        requireUserAndDevice(
                userId,
                deviceInstallId);

        Instant failedAt = timeProvider.now();

        Optional<PinResetAttempt> existingAttempt = repository
                .findForUpdateByUserIdAndDeviceInstallId(
                        userId,
                        deviceInstallId);

        if (existingAttempt.isEmpty()) {
            return createFirstFailure(
                    userId,
                    deviceInstallId,
                    failedAt);
        }

        return processExistingAttempt(
                existingAttempt.get(),
                failedAt);
    }

    private PinResetAttempt processExistingAttempt(
            PinResetAttempt attempt,
            Instant failedAt) {
        if (attempt.hasPinBeenIssued()) {
            return attempt;
        }

        if (attempt.isExpired(failedAt)) {
            repository.deleteById(
                    attempt.getId());

            return createFirstFailure(
                    attempt.getUserId(),
                    attempt.getDeviceInstallId(),
                    failedAt);
        }

        if (attempt.getStatus().isFirstFailure()) {
            Instant resetExpiresAt = lifetime.calculateResetExpiresAt(
                    failedAt);

            PinResetAttempt resetAvailable = attempt.recordSecondFailure(
                    failedAt,
                    resetExpiresAt);

            return repository.save(
                    resetAvailable);
        }

        return attempt;
    }

    private PinResetAttempt createFirstFailure(
            UUID userId,
            UUID deviceInstallId,
            Instant failedAt) {
        Instant expiresAt = lifetime
                .calculateFirstFailureExpiresAt(
                        failedAt);

        PinResetAttempt newAttempt = PinResetAttempt
                .createAfterFirstFailure(
                        userId,
                        deviceInstallId,
                        failedAt,
                        expiresAt);

        boolean created = repository
                .createFirstFailureIfAbsent(
                        newAttempt);

        if (created) {
            return newAttempt;
        }

        return repository
                .findForUpdateByUserIdAndDeviceInstallId(
                        userId,
                        deviceInstallId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "PIN-reset attempt was not found after concurrent creation"));
    }

    private static void requireUserAndDevice(
            UUID userId,
            UUID deviceInstallId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId cannot be null");
        }

        if (deviceInstallId == null) {
            throw new IllegalArgumentException(
                    "deviceInstallId cannot be null");
        }
    }
}