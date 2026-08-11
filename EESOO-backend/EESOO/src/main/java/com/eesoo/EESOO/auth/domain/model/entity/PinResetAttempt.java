package com.eesoo.EESOO.auth.domain.model.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.enums.PinResetStatus;
import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptId;

public class PinResetAttempt {

    private static final int MAX_FAILED_ATTEMPTS = 2;

     private final PinResetAttemptId id;

    private final UUID userId;
    private final UUID deviceInstallId;

    private final int failedAttempts;
    private final PinResetStatus status;

    private final Instant createdAt;
    private final Instant expiresAt;

    private final Instant mobileConfirmedAt;
    private final Instant pinIssuedAt;

    private final Long version;

    private PinResetAttempt(
            PinResetAttemptId id,
            UUID userId,
            UUID deviceInstallId,
            int failedAttempts,
            PinResetStatus status,
            Instant createdAt,
            Instant expiresAt,
            Instant mobileConfirmedAt,
            Instant pinIssuedAt,
            Long version
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "pinResetAttemptId cannot be null"
            );
        }

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

        if (failedAttempts < 1
                || failedAttempts > MAX_FAILED_ATTEMPTS) {
            throw new IllegalArgumentException(
                    "failedAttempts must be between 1 and "
                            + MAX_FAILED_ATTEMPTS
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "status cannot be null"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "createdAt cannot be null"
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "expiresAt cannot be null"
            );
        }

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "expiresAt must be after createdAt"
            );
        }

        if (version != null && version < 0) {
            throw new IllegalArgumentException(
                    "version cannot be negative"
            );
        }

        validateState(
                failedAttempts,
                status,
                createdAt,
                expiresAt,
                mobileConfirmedAt,
                pinIssuedAt
        );

        this.id = id;
        this.userId = userId;
        this.deviceInstallId = deviceInstallId;
        this.failedAttempts = failedAttempts;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.mobileConfirmedAt = mobileConfirmedAt;
        this.pinIssuedAt = pinIssuedAt;
        this.version = version;
    }

    /*
     * Called when the user enters the first wrong PIN.
     */
    public static PinResetAttempt createAfterFirstFailure(
            UUID userId,
            UUID deviceInstallId,
            Instant failedAt,
            Instant expiresAt
    ) {
        requireTime(failedAt, "failedAt");
        requireTime(expiresAt, "expiresAt");

        if (!expiresAt.isAfter(failedAt)) {
            throw new IllegalArgumentException(
                    "expiresAt must be after failedAt"
            );
        }

        return new PinResetAttempt(
                PinResetAttemptId.create(),
                userId,
                deviceInstallId,
                1,
                PinResetStatus.FIRST_FAILURE,
                failedAt,
                expiresAt,
                null,
                null,
                null
        );
    }

    /*
     * Called by the persistence mapper when an existing
     * database row is converted back into a domain object.
     */
    public static PinResetAttempt rehydrate(
            PinResetAttemptId id,
            UUID userId,
            UUID deviceInstallId,
            int failedAttempts,
            PinResetStatus status,
            Instant createdAt,
            Instant expiresAt,
            Instant mobileConfirmedAt,
            Instant pinIssuedAt,
            Long version
    ) {
        return new PinResetAttempt(
                id,
                userId,
                deviceInstallId,
                failedAttempts,
                status,
                createdAt,
                expiresAt,
                mobileConfirmedAt,
                pinIssuedAt,
                version
        );
    }

    /*
     * Called when the user enters the second wrong PIN.
     *
     * FIRST_FAILURE becomes RESET_AVAILABLE.
     */
    public PinResetAttempt recordSecondFailure(
            Instant failedAt,
            Instant resetExpiresAt
    ) {
        requireTime(failedAt, "failedAt");
        requireTime(resetExpiresAt, "resetExpiresAt");

        requireNotExpired(failedAt);

        if (status != PinResetStatus.FIRST_FAILURE) {
            throw new IllegalStateException(
                    "Second failure can only be recorded after the first failure"
            );
        }

        if (!resetExpiresAt.isAfter(failedAt)) {
            throw new IllegalArgumentException(
                    "resetExpiresAt must be after failedAt"
            );
        }

        return new PinResetAttempt(
                id,
                userId,
                deviceInstallId,
                2,
                PinResetStatus.RESET_AVAILABLE,
                createdAt,
                resetExpiresAt,
                null,
                null,
                version
        );
    }

    /*
     * Called after the entered phone number matches
     * the phone number registered to this user.
     */
    public PinResetAttempt confirmMobile(
            Instant confirmedAt
    ) {
        requireTime(confirmedAt, "confirmedAt");
        requireNotExpired(confirmedAt);

        if (status != PinResetStatus.RESET_AVAILABLE) {
            throw new IllegalStateException(
                    "Mobile can only be confirmed when reset is available"
            );
        }

        return new PinResetAttempt(
                id,
                userId,
                deviceInstallId,
                failedAttempts,
                PinResetStatus.MOBILE_CONFIRMED,
                createdAt,
                expiresAt,
                confirmedAt,
                null,
                version
        );
    }

    /*
     * Called only after the new PIN is generated,
     * hashed and successfully saved in users.pin.
     */
    public PinResetAttempt markPinIssued(
            Instant issuedAt
    ) {
        requireTime(issuedAt, "issuedAt");
        requireNotExpired(issuedAt);

        if (status != PinResetStatus.MOBILE_CONFIRMED) {
            throw new IllegalStateException(
                    "PIN can only be issued after mobile confirmation"
            );
        }

        return new PinResetAttempt(
                id,
                userId,
                deviceInstallId,
                failedAttempts,
                PinResetStatus.PIN_ISSUED,
                createdAt,
                expiresAt,
                mobileConfirmedAt,
                issuedAt,
                version
        );
    }

    public boolean isExpired(
            Instant currentTime
    ) {
        requireTime(currentTime, "currentTime");

        return !currentTime.isBefore(expiresAt);
    }

    public boolean isResetAvailable(
            Instant currentTime
    ) {
        return !isExpired(currentTime)
                && status.isResetAvailable();
    }

    public boolean isReadyForPinReset(
            Instant currentTime
    ) {
        return !isExpired(currentTime)
                && status.isMobileConfirmed();
    }

    public boolean hasPinBeenIssued() {
        return status.isPinIssued();
    }

    private void requireNotExpired(
            Instant currentTime
    ) {
        if (isExpired(currentTime)) {
            throw new IllegalStateException(
                    "PIN reset attempt has expired"
            );
        }
    }

    private static void validateState(
            int failedAttempts,
            PinResetStatus status,
            Instant createdAt,
            Instant expiresAt,
            Instant mobileConfirmedAt,
            Instant pinIssuedAt
    ) {
        switch (status) {
            case FIRST_FAILURE -> {
                require(
                        failedAttempts == 1,
                        "FIRST_FAILURE requires exactly one failure"
                );

                require(
                        mobileConfirmedAt == null,
                        "FIRST_FAILURE cannot have mobileConfirmedAt"
                );

                require(
                        pinIssuedAt == null,
                        "FIRST_FAILURE cannot have pinIssuedAt"
                );
            }

            case RESET_AVAILABLE -> {
                require(
                        failedAttempts == 2,
                        "RESET_AVAILABLE requires exactly two failures"
                );

                require(
                        mobileConfirmedAt == null,
                        "RESET_AVAILABLE cannot have mobileConfirmedAt"
                );

                require(
                        pinIssuedAt == null,
                        "RESET_AVAILABLE cannot have pinIssuedAt"
                );
            }

            case MOBILE_CONFIRMED -> {
                require(
                        failedAttempts == 2,
                        "MOBILE_CONFIRMED requires exactly two failures"
                );

                require(
                        mobileConfirmedAt != null,
                        "MOBILE_CONFIRMED requires mobileConfirmedAt"
                );

                require(
                        pinIssuedAt == null,
                        "MOBILE_CONFIRMED cannot have pinIssuedAt"
                );
            }

            case PIN_ISSUED -> {
                require(
                        failedAttempts == 2,
                        "PIN_ISSUED requires exactly two failures"
                );

                require(
                        mobileConfirmedAt != null,
                        "PIN_ISSUED requires mobileConfirmedAt"
                );

                require(
                        pinIssuedAt != null,
                        "PIN_ISSUED requires pinIssuedAt"
                );
            }
        }

        validateEventTime(
                mobileConfirmedAt,
                "mobileConfirmedAt",
                createdAt,
                expiresAt
        );

        validateEventTime(
                pinIssuedAt,
                "pinIssuedAt",
                createdAt,
                expiresAt
        );

        if (mobileConfirmedAt != null
                && pinIssuedAt != null
                && pinIssuedAt.isBefore(mobileConfirmedAt)) {
            throw new IllegalArgumentException(
                    "pinIssuedAt cannot be before mobileConfirmedAt"
            );
        }
    }

    private static void validateEventTime(
            Instant eventTime,
            String fieldName,
            Instant createdAt,
            Instant expiresAt
    ) {
        if (eventTime == null) {
            return;
        }

        if (eventTime.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be before createdAt"
            );
        }

        if (!eventTime.isBefore(expiresAt)) {
            throw new IllegalArgumentException(
                    fieldName + " must be before expiresAt"
            );
        }
    }

    private static void require(
            boolean condition,
            String message
    ) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requireTime(
            Instant value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }
    }

    public PinResetAttemptId getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getDeviceInstallId() {
        return deviceInstallId;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public int getRemainingAttempts() {
        return Math.max(
                0,
                MAX_FAILED_ATTEMPTS
                        - failedAttempts
        );
    }

    public PinResetStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getMobileConfirmedAt() {
        return mobileConfirmedAt;
    }

    public Instant getPinIssuedAt() {
        return pinIssuedAt;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PinResetAttempt)) {
            return false;
        }

        PinResetAttempt that =
                (PinResetAttempt) object;

        return Objects.equals(
                id,
                that.id
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
}
