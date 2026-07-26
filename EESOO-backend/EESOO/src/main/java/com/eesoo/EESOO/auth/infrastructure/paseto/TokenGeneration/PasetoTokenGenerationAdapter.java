package com.eesoo.EESOO.auth.infrastructure.paseto.TokenGeneration;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.model.token.AccessTokenClaims;
import com.eesoo.EESOO.auth.domain.model.token.RefreshTokenClaims;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;
import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;
import com.eesoo.EESOO.auth.domain.port.TokenGenerationPort;
import com.eesoo.EESOO.auth.infrastructure.paseto.PasetoKeyProvider;
import com.eesoo.EESOO.auth.infrastructure.paseto.config.PasetoProperties;

import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.lang.Keys;

@Component
public class PasetoTokenGenerationAdapter implements TokenGenerationPort {

    private static final String SESSION_ID_CLAIM = "sid";

    private static final String  TOKEN_TYPE_CLAIM = "token_type";

     private final SecretKey sharedSecret;
    private final PasetoProperties properties;
    private final Clock clock;

    public  PasetoTokenGenerationAdapter(
            PasetoKeyProvider keyProvider,
            PasetoProperties properties,
            Clock clock
    ) {
        if (keyProvider == null) {
            throw new IllegalArgumentException(
                    "keyProvider cannot be null"
            );
        }

        if (properties == null) {
            throw new IllegalArgumentException(
                    "properties cannot be null"
            );
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "clock cannot be null"
            );
        }

        this.sharedSecret = Keys.secretKey(
                keyProvider.getKeyBytes()
        );

        this.properties = properties;
        this.clock = clock;
    }


    @Override
    public TokenPair generateTokens(
            UUID userId,
            AuthSessionId authSessionId,
            Instant sessionExpiresAt
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId cannot be null"
            );
        }

        if (authSessionId == null) {
            throw new IllegalArgumentException(
                    "authSessionId cannot be null"
            );
        }

        if (sessionExpiresAt == null) {
            throw new IllegalArgumentException(
                    "sessionExpiresAt cannot be null"
            );
        }



    Instant issuedAt =
                clock.instant();

        if (!sessionExpiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException(
                    "sessionExpiresAt must be after issuedAt"
            );
        }

        Instant accessTokenExpiresAt =
                calculateAccessTokenExpiration(
                        issuedAt,
                        sessionExpiresAt
                );

        AccessTokenClaims accessClaims =
                new AccessTokenClaims(
                        properties.getIssuer(),
                        userId,
                        properties.getAccessAudience(),
                        issuedAt,
                        accessTokenExpiresAt,
                        UUID.randomUUID(),
                        authSessionId
                );

        RefreshTokenClaims refreshClaims =
                new RefreshTokenClaims(
                        properties.getIssuer(),
                        userId,
                        properties.getRefreshAudience(),
                        issuedAt,
                        sessionExpiresAt,
                        UUID.randomUUID(),
                        authSessionId
                );

        String accessToken =
                generateAccessToken(accessClaims);

        String refreshToken =
                generateRefreshToken(refreshClaims);

        return new TokenPair(
                accessToken,
                refreshToken
        );
    }

    private Instant calculateAccessTokenExpiration(
            Instant issuedAt,
            Instant sessionExpiresAt
    ) {
        Instant normalAccessExpiration =
                issuedAt.plus(
                        properties.getAccessTokenDuration()
                );

        if (normalAccessExpiration.isBefore(
                sessionExpiresAt
        )) {
            return normalAccessExpiration;
        }

        return sessionExpiresAt;
    }

    private String generateAccessToken(
            AccessTokenClaims claims
    ) {
        return Pasetos.V2.LOCAL
                .builder()
                .setSharedSecret(sharedSecret)
                .setIssuer(claims.getIssuer())
                .setSubject(
                        claims.getUserId().toString()
                )
                .setAudience(claims.getAudience())
                .setIssuedAt(claims.getIssuedAt())
                .setExpiration(claims.getExpiresAt())
                .setTokenId(
                        claims.getTokenId().toString()
                )
                .claim(
                        SESSION_ID_CLAIM,
                        claims.getSessionId().toString()
                )
                .claim(
                        TOKEN_TYPE_CLAIM,
                        claims.getTokenType()
                )
                .compact();
    }

    private String generateRefreshToken(
            RefreshTokenClaims claims
    ) {
        return Pasetos.V2.LOCAL
                .builder()
                .setSharedSecret(sharedSecret)
                .setIssuer(claims.getIssuer())
                .setSubject(
                        claims.getUserId().toString()
                )
                .setAudience(claims.getAudience())
                .setIssuedAt(claims.getIssuedAt())
                .setExpiration(claims.getExpiresAt())
                .setTokenId(
                        claims.getTokenId().toString()
                )
                .claim(
                        SESSION_ID_CLAIM,
                        claims.getSessionId().toString()
                )
                .claim(
                        TOKEN_TYPE_CLAIM,
                        claims.getTokenType()
                )
                .compact();
    }
    
}


