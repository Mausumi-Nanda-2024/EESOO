package com.eesoo.EESOO.auth.infrastructure.paseto.TokenGeneration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;
import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;
import com.eesoo.EESOO.auth.infrastructure.paseto.PasetoKeyProvider;
import com.eesoo.EESOO.auth.infrastructure.paseto.config.PasetoProperties;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.Pasetos;
import dev.paseto.jpaseto.lang.Keys;

class PasetoTokenGenerationAdapterTest {

    private static final Instant NOW =
            Instant.parse("2026-07-25T10:00:00Z");

    private static final Duration ACCESS_TOKEN_DURATION =
            Duration.ofMinutes(15);

    private Clock clock;
    private SecretKey sharedSecret;
    private PasetoTokenGenerationAdapter adapter;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[32];
        Arrays.fill(keyBytes, (byte) 7);

        String encodedKey =
                Base64.getEncoder().encodeToString(keyBytes);

        PasetoKeyProvider keyProvider =
                new PasetoKeyProvider(encodedKey);

        PasetoProperties properties =
                new PasetoProperties(
                        encodedKey,
                        "eesoo",
                        "eesoo-api",
                        "eesoo-auth-refresh",
                        ACCESS_TOKEN_DURATION
                );

        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        sharedSecret = Keys.secretKey(keyProvider.getKeyBytes());

        adapter = new PasetoTokenGenerationAdapter(
                keyProvider,
                properties,
                clock
        );
    }

    @Test
    void generateTokensCreatesTokensWithExpectedClaims() {
        UUID userId =
                UUID.fromString("018f47d2-f9c3-7f4d-9a21-91d42f64c101");

        AuthSessionId sessionId =
                AuthSessionId.fromString(
                        "018f47d2-f9c3-7f4d-9a21-91d42f64c102"
                );

        Instant sessionExpiresAt =
                NOW.plus(Duration.ofDays(30));

        TokenPair tokenPair =
                adapter.generateTokens(
                        userId,
                        sessionId,
                        sessionExpiresAt
                );

        assertTrue(tokenPair.getAccessToken().startsWith("v2.local."));
        assertTrue(tokenPair.getRefreshToken().startsWith("v2.local."));

        Paseto accessToken = parse(tokenPair.getAccessToken());
        Paseto refreshToken = parse(tokenPair.getRefreshToken());

        assertEquals("eesoo", accessToken.getClaims().getIssuer());
        assertEquals(userId.toString(), accessToken.getClaims().getSubject());
        assertEquals("eesoo-api", accessToken.getClaims().getAudience());
        assertEquals(NOW, accessToken.getClaims().getIssuedAt());
        assertEquals(
                NOW.plus(ACCESS_TOKEN_DURATION),
                accessToken.getClaims().getExpiration()
        );
        assertEquals(
                sessionId.toString(),
                accessToken.getClaims().get("sid", String.class)
        );
        assertEquals(
                "access",
                accessToken.getClaims().get("token_type", String.class)
        );

        assertEquals("eesoo", refreshToken.getClaims().getIssuer());
        assertEquals(userId.toString(), refreshToken.getClaims().getSubject());
        assertEquals(
                "eesoo-auth-refresh",
                refreshToken.getClaims().getAudience()
        );
        assertEquals(NOW, refreshToken.getClaims().getIssuedAt());
        assertEquals(
                sessionExpiresAt,
                refreshToken.getClaims().getExpiration()
        );
        assertEquals(
                sessionId.toString(),
                refreshToken.getClaims().get("sid", String.class)
        );
        assertEquals(
                "refresh",
                refreshToken.getClaims().get("token_type", String.class)
        );

        assertNotEquals(
                accessToken.getClaims().getTokenId(),
                refreshToken.getClaims().getTokenId()
        );
        assertFalse(accessToken.getClaims().containsKey("username"));
        assertFalse(refreshToken.getClaims().containsKey("username"));
    }

    @Test
    void generateTokensClampsAccessExpiryToSessionExpiry() {
        Instant sessionExpiresAt =
                NOW.plus(Duration.ofMinutes(5));

        TokenPair tokenPair =
                adapter.generateTokens(
                        UUID.randomUUID(),
                        AuthSessionId.create(),
                        sessionExpiresAt
                );

        Paseto accessToken = parse(tokenPair.getAccessToken());
        Paseto refreshToken = parse(tokenPair.getRefreshToken());

        assertEquals(
                sessionExpiresAt,
                accessToken.getClaims().getExpiration()
        );
        assertEquals(
                sessionExpiresAt,
                refreshToken.getClaims().getExpiration()
        );
    }

    @Test
    void generateTokensRejectsExpiredSessionBoundary() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.generateTokens(
                        UUID.randomUUID(),
                        AuthSessionId.create(),
                        NOW
                )
        );
    }

    private Paseto parse(String token) {
        return Pasetos.parserBuilder()
                .setSharedSecret(sharedSecret)
                .setClock(clock)
                .build()
                .parse(token);
    }
}
