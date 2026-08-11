package com.eesoo.EESOO.auth.domain.model.token;

import java.time.Instant;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;

public final class AccessTokenClaims {

    private static final String TOKEN_TYPE =
            "access";

    private final String issuer;
    private final UUID userId;
    private final String audience;

    private final Instant issuedAt;
    private final Instant expiresAt;

    private final UUID tokenId;
    private final AuthSessionId sessionId;

    public AccessTokenClaims(
            String issuer,
            UUID userId,
            String audience,
            Instant issuedAt,
            Instant expiresAt,
            UUID tokenId,
            AuthSessionId sessionId
    ) {
        if (issuer == null
                || issuer.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "issuer cannot be null or blank"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId cannot be null"
            );
        }

        if (audience == null
                || audience.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "audience cannot be null or blank"
            );
        }

        if (issuedAt == null) {
            throw new IllegalArgumentException(
                    "issuedAt cannot be null"
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "expiresAt cannot be null"
            );
        }

        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException(
                    "expiresAt must be after issuedAt"
            );
        }

        if (tokenId == null) {
            throw new IllegalArgumentException(
                    "tokenId cannot be null"
            );
        }

        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "sessionId cannot be null"
            );
        }

        this.issuer = issuer.trim();
        this.userId = userId;
        this.audience = audience.trim();
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.tokenId = tokenId;
        this.sessionId = sessionId;
    }

    public String getIssuer() {
        return issuer;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAudience() {
        return audience;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public AuthSessionId getSessionId() {
        return sessionId;
    }

    public String getTokenType() {
        return TOKEN_TYPE;
    }
}