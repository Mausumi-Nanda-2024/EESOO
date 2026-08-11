package com.eesoo.EESOO.auth.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eesoo.EESOO.auth.infrastructure.persistence.entity.PinResetAttemptJpaEntity;

import jakarta.persistence.LockModeType;

public interface SpringDataPinResetAttemptRepository
        extends JpaRepository<
                PinResetAttemptJpaEntity,
                UUID
        > {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT attempt
            FROM PinResetAttemptJpaEntity attempt
            WHERE attempt.id = :attemptId
            """)
    Optional<PinResetAttemptJpaEntity>
            findForUpdateById(
                    @Param("attemptId")
                    UUID attemptId
            );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT attempt
            FROM PinResetAttemptJpaEntity attempt
            WHERE attempt.userId = :userId
              AND attempt.deviceInstallId = :deviceInstallId
            """)
    Optional<PinResetAttemptJpaEntity>
            findForUpdateByUserIdAndDeviceInstallId(
                    @Param("userId")
                    UUID userId,

                    @Param("deviceInstallId")
                    UUID deviceInstallId
            );

    @Modifying(flushAutomatically = true)
    @Query(
            value = """
                    INSERT INTO pin_reset_attempts (
                        id,
                        user_id,
                        device_install_id,
                        failed_attempts,
                        status,
                        created_at,
                        expires_at,
                        mobile_confirmed_at,
                        pin_issued_at,
                        version
                    )
                    VALUES (
                        :id,
                        :userId,
                        :deviceInstallId,
                        1,
                        'FIRST_FAILURE',
                        :createdAt,
                        :expiresAt,
                        NULL,
                        NULL,
                        0
                    )
                    ON CONFLICT (
                        user_id,
                        device_install_id
                    )
                    DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertFirstFailureIfAbsent(
            @Param("id")
            UUID id,

            @Param("userId")
            UUID userId,

            @Param("deviceInstallId")
            UUID deviceInstallId,

            @Param("createdAt")
            Instant createdAt,

            @Param("expiresAt")
            Instant expiresAt
    );

    void deleteByUserIdAndDeviceInstallId(
            UUID userId,
            UUID deviceInstallId
    );
}