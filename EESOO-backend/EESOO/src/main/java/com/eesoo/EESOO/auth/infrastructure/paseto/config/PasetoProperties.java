package com.eesoo.EESOO.auth.infrastructure.paseto.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.paseto")
public class PasetoProperties {
    

    private final String localKeyBase64;
    private final String issuer;
    private final String accessAudience;
    private final String refreshAudience;
    private final Duration accessTokenDuration;

    public PasetoProperties(
            String localKeyBase64,
            String issuer,
            String accessAudience,
            String refreshAudience,
            Duration accessTokenDuration
    ) {
        if (isBlank(localKeyBase64)) {
            throw new IllegalArgumentException(
                    "PASETO local key cannot be null or blank"
            );
        }

        if (isBlank(issuer)) {
            throw new IllegalArgumentException(
                    "PASETO issuer cannot be null or blank"
            );
        }

        if (isBlank(accessAudience)) {
            throw new IllegalArgumentException(
                    "Access-token audience cannot be null or blank"
            );
        }

        if (isBlank(refreshAudience)) {
            throw new IllegalArgumentException(
                    "Refresh-token audience cannot be null or blank"
            );
        }

        validateDuration(
                accessTokenDuration,
                "accessTokenDuration"
        );

        this.localKeyBase64 =
                localKeyBase64.trim();

        this.issuer = issuer.trim();

        this.accessAudience =
                accessAudience.trim();

        this.refreshAudience =
                refreshAudience.trim();

        this.accessTokenDuration =
                accessTokenDuration;
    }

    private static void validateDuration(
            Duration duration,
            String fieldName
    ) {
        if (duration == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }

        if (duration.isZero()
                || duration.isNegative()) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }
    }

    private static boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }

    public String getLocalKeyBase64() {
        return localKeyBase64;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAccessAudience() {
        return accessAudience;
    }

    public String getRefreshAudience() {
        return refreshAudience;
    }

    public Duration getAccessTokenDuration() {
        return accessTokenDuration;
    }

}
