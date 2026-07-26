package com.eesoo.EESOO.auth.domain.model.entity;



import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


import com.eesoo.EESOO.auth.domain.model.enums.AuthSessionStatus;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;
import com.eesoo.EESOO.auth.domain.model.valueobject.RefreshTokenHash;

public class AuthSession {

    private final AuthSessionId id;
    private final UUID userId;
    private final UUID deviceInstallId;

    private final RefreshTokenHash refreshTokenHash;
    private final AuthSessionStatus status;

    private final Instant createdAt;
    private final Instant expiresAt;
    private final Instant lastRotatedAt;
    private final Instant revokedAt;

    private final Long version;

    private AuthSession(
            AuthSessionId id,
            UUID userId,
            UUID deviceInstallId,
            RefreshTokenHash refreshTokenHash,
            AuthSessionStatus status,
            Instant createdAt,
            Instant expiresAt,
            Instant lastRotatedAt,
            Instant revokedAt,
            Long version
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "authSessionId cannot be null"
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

        if (refreshTokenHash == null) {
            throw new IllegalArgumentException(
                    "refreshTokenHash cannot be null"
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

        validateLastRotatedAt(
                createdAt,
                expiresAt,
                lastRotatedAt
        );

        validateRevokedAt(
                status,
                createdAt,
                revokedAt
        );

        if (version != null && version < 0) {
            throw new IllegalArgumentException(
                    "version cannot be negative"
            );
        }

        this.id = id;
        this.userId = userId;
        this.deviceInstallId = deviceInstallId;
        this.refreshTokenHash = refreshTokenHash;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.lastRotatedAt = lastRotatedAt;
        this.revokedAt = revokedAt;
        this.version = version;
    }

    public static AuthSession createActive(
            AuthSessionId id,
            UUID userId,
            UUID deviceInstallId,
            RefreshTokenHash refreshTokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new AuthSession(
                id,
                userId,
                deviceInstallId,
                refreshTokenHash,
                AuthSessionStatus.ACTIVE,
                createdAt,
                expiresAt,
                null,
                null,
                null
        );
    }

    public static AuthSession rehydrate(
            AuthSessionId id,
            UUID userId,
            UUID deviceInstallId,
            RefreshTokenHash refreshTokenHash,
            AuthSessionStatus status,
            Instant createdAt,
            Instant expiresAt,
            Instant lastRotatedAt,
            Instant revokedAt,
            Long version
    ) {
        return new AuthSession(
                id,
                userId,
                deviceInstallId,
                refreshTokenHash,
                status,
                createdAt,
                expiresAt,
                lastRotatedAt,
                revokedAt,
                version
        );
    }

    public AuthSession rotateRefreshToken(
            RefreshTokenHash newRefreshTokenHash,
            Instant rotatedAt
    ) {
        if (newRefreshTokenHash == null) {
            throw new IllegalArgumentException(
                    "newRefreshTokenHash cannot be null"
            );
        }

        if (refreshTokenHash.equals(newRefreshTokenHash)) {
            throw new IllegalArgumentException(
                    "New refresh token hash must be different"
            );
        }

        requireTime(rotatedAt, "rotatedAt");

        if (!isUsableAt(rotatedAt)) {
            throw new IllegalStateException(
                    "Refresh token cannot be rotated for an unusable session"
            );
        }

        return new AuthSession(
                id,
                userId,
                deviceInstallId,
                newRefreshTokenHash,
                AuthSessionStatus.ACTIVE,
                createdAt,
                expiresAt,
                rotatedAt,
                null,
                version
        );
    }

    public AuthSession revoke(
            Instant revokedAt
    ) {
        requireTime(revokedAt, "revokedAt");

        if (status == AuthSessionStatus.REVOKED) {
            return this;
        }

        if (status == AuthSessionStatus.EXPIRED) {
            return this;
        }

        return new AuthSession(
                id,
                userId,
                deviceInstallId,
                refreshTokenHash,
                AuthSessionStatus.REVOKED,
                createdAt,
                expiresAt,
                lastRotatedAt,
                revokedAt,
                version
        );
    }

    public AuthSession expire(
            Instant currentTime
    ) {
        requireTime(currentTime, "currentTime");

        if (status == AuthSessionStatus.EXPIRED) {
            return this;
        }

        if (status == AuthSessionStatus.REVOKED) {
            return this;
        }

        if (currentTime.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "Session has not reached its expiration time"
            );
        }

        return new AuthSession(
                id,
                userId,
                deviceInstallId,
                refreshTokenHash,
                AuthSessionStatus.EXPIRED,
                createdAt,
                expiresAt,
                lastRotatedAt,
                null,
                version
        );
    }

    public boolean hasExpired(
            Instant currentTime
    ) {
        requireTime(currentTime, "currentTime");

        return hasExpiredAt(currentTime);
    }

    public boolean isUsable(
            Instant currentTime
    ) {
        requireTime(currentTime, "currentTime");

        return isUsableAt(currentTime);
    }

    private boolean hasExpiredAt(
            Instant currentTime
    ) {
        return !currentTime.isBefore(expiresAt);
    }

    private boolean isUsableAt(
            Instant currentTime
    ) {
        return status.isActive()
                && !hasExpiredAt(currentTime);
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

    private static void validateLastRotatedAt(
            Instant createdAt,
            Instant expiresAt,
            Instant lastRotatedAt
    ) {
        if (lastRotatedAt == null) {
            return;
        }

        if (lastRotatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "lastRotatedAt cannot be before createdAt"
            );
        }

        if (!lastRotatedAt.isBefore(expiresAt)) {
            throw new IllegalArgumentException(
                    "lastRotatedAt must be before expiresAt"
            );
        }
    }

    private static void validateRevokedAt(
            AuthSessionStatus status,
            Instant createdAt,
            Instant revokedAt
    ) {
        if (status == AuthSessionStatus.REVOKED
                && revokedAt == null) {
            throw new IllegalArgumentException(
                    "Revoked session must have revokedAt"
            );
        }

        if (status != AuthSessionStatus.REVOKED
                && revokedAt != null) {
            throw new IllegalArgumentException(
                    "Only a revoked session can have revokedAt"
            );
        }

        if (revokedAt != null
                && revokedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "revokedAt cannot be before createdAt"
            );
        }
    }

    public AuthSessionId getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getDeviceInstallId() {
        return deviceInstallId;
    }

    public RefreshTokenHash getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public AuthSessionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastRotatedAt() {
        return lastRotatedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (!(object instanceof AuthSession)) {
            return false;
        }

        AuthSession authSession =
                (AuthSession) object;

        return Objects.equals(
                id,
                authSession.id
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
