package com.eesoo.EESOO.auth.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptId;

public interface PinResetAttemptRepository {

    boolean createFirstFailureIfAbsent(
            PinResetAttempt attempt);

    PinResetAttempt save(
            PinResetAttempt attempt);

    Optional<PinResetAttempt> findById(
            PinResetAttemptId attemptId);

    Optional<PinResetAttempt> findForUpdateById(
            PinResetAttemptId attemptId);

    Optional<PinResetAttempt> findForUpdateByUserIdAndDeviceInstallId(
            UUID userId,
            UUID deviceInstallId);

    void deleteById(
            PinResetAttemptId attemptId);

    void deleteByUserIdAndDeviceInstallId(
            UUID userId,
            UUID deviceInstallId);
}
