package com.eesoo.EESOO.auth.infrastructure.paseto;

import java.util.Arrays;
import java.util.Base64;

public final class PasetoKeyProvider {

   
    private static final int REQUIRED_KEY_LENGTH =
            32;

    private final byte[] keyBytes;

    public PasetoKeyProvider(
            String base64EncodedKey
    ) {
        if (base64EncodedKey == null
                || base64EncodedKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "PASETO key cannot be null or blank"
            );
        }

        byte[] decodedKey =
                decodeKey(base64EncodedKey.trim());

        if (decodedKey.length
                != REQUIRED_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "PASETO local key must contain exactly 32 bytes"
            );
        }

        this.keyBytes = Arrays.copyOf(
                decodedKey,
                decodedKey.length
        );
    }

    public byte[] getKeyBytes() {
        return Arrays.copyOf(
                keyBytes,
                keyBytes.length
        );
    }

    private static byte[] decodeKey(
            String base64EncodedKey
    ) {
        try {
            return Base64.getDecoder()
                    .decode(base64EncodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "PASETO key must be valid Base64",
                    exception
            );
        }
    }
}
