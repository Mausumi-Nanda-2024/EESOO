package com.eesoo.EESOO.auth.infrastructure.paseto.TokenValidation;

import java.time.Clock;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.exception.InvalidTokenException;
import com.eesoo.EESOO.auth.domain.model.token.AccessTokenClaims;
import com.eesoo.EESOO.auth.domain.model.token.RefreshTokenClaims;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;
import com.eesoo.EESOO.auth.domain.port.TokenValidationPort;
import com.eesoo.EESOO.auth.infrastructure.paseto.PasetoKeyProvider;
import com.eesoo.EESOO.auth.infrastructure.paseto.config.PasetoProperties;

import dev.paseto.jpaseto.Claims;
import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.lang.Keys;

@Component
public class PasetoTokenValidationAdapter implements TokenValidationPort {

    private static final String SESSION_ID_CLAIM = "sid";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";


    private final SecretKey sharedSecret;
    private final PasetoProperties properties;
    private final Clock clock;

      public PasetoTokenValidationAdapter(
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

      public AccessTokenClaims validateAccessToken(
            String rawAccessToken
    ) {
        Claims claims = parseClaims(rawAccessToken);

        validateCommonClaims(
                claims,
                properties.getAccessAudience(),
                ACCESS_TOKEN_TYPE
        );

        return new AccessTokenClaims(
                claims.getIssuer(),
                parseUserId(claims),
                claims.getAudience(),
                claims.getIssuedAt(),
                claims.getExpiration(),
                parseTokenId(claims),
                parseSessionId(claims)
        );
    }

    @Override
    public RefreshTokenClaims validateRefreshToken(
            String rawRefreshToken
    ) {
        Claims claims = parseClaims(rawRefreshToken);

        validateCommonClaims(
                claims,
                properties.getRefreshAudience(),
                REFRESH_TOKEN_TYPE
        );

        return new RefreshTokenClaims(
                claims.getIssuer(),
                parseUserId(claims),
                claims.getAudience(),
                claims.getIssuedAt(),
                claims.getExpiration(),
                parseTokenId(claims),
                parseSessionId(claims)
        );
    }

    private Claims parseClaims(String rawToken) {
        if (isBlank(rawToken)) {
            throw new InvalidTokenException();
        }

        try {
            Paseto paseto =
                    Pasetos.parserBuilder()
                            .setSharedSecret(sharedSecret)
                            .setClock(clock)
                            .build()
                            .parse(rawToken.trim());

            return paseto.getClaims();

        } catch (RuntimeException exception) {
            throw new InvalidTokenException(
                    exception
            );
        }
    }

    private void validateCommonClaims(
            Claims claims,
            String expectedAudience,
            String expectedTokenType
    ) {
        if (!properties.getIssuer().equals(
                claims.getIssuer()
        )) {
            throw new InvalidTokenException();
        }

        if (!expectedAudience.equals(
                claims.getAudience()
        )) {
            throw new InvalidTokenException();
        }

        String actualTokenType;

        try {
            actualTokenType =
                    claims.get(
                            TOKEN_TYPE_CLAIM,
                            String.class
                    );
        } catch (RuntimeException exception) {
            throw new InvalidTokenException(
                    exception
            );
        }

        if (!expectedTokenType.equals(
                actualTokenType
        )) {
            throw new InvalidTokenException();
        }
    }

    private UUID parseUserId(Claims claims) {
        try {
            return UUID.fromString(
                    claims.getSubject()
            );
        } catch (RuntimeException exception) {
            throw new InvalidTokenException(
                    exception
            );
        }
    }

    private UUID parseTokenId(Claims claims) {
        try {
            return UUID.fromString(
                    claims.getTokenId()
            );
        } catch (RuntimeException exception) {
            throw new InvalidTokenException(
                    exception
            );
        }
    }

    private AuthSessionId parseSessionId(
            Claims claims
    ) {
        try {
            String sessionId =
                    claims.get(
                            SESSION_ID_CLAIM,
                            String.class
                    );

            return AuthSessionId.fromString(
                    sessionId
            );
        } catch (RuntimeException exception) {
            throw new InvalidTokenException(
                    exception
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }



    

    
    
}
