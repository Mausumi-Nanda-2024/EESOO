package com.eesoo.EESOO.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.model.valueobject.RefreshTokenHash;
import com.eesoo.EESOO.auth.domain.port.RefreshTokenHasherPort;


@Component
public class RefreshTokenHasherAdapter implements RefreshTokenHasherPort {

     private static final String HASH_ALGORITHM =
            "SHA-256";

    @Override
    public RefreshTokenHash hash(
            String rawRefreshToken
    ) {
        validateRawRefreshToken(rawRefreshToken);

        byte[] tokenHash =
                calculateHash(rawRefreshToken);

        String encodedHash =
                HexFormat.of().formatHex(tokenHash);

        return RefreshTokenHash.of(encodedHash);
    }

    @Override
    public boolean matches(
            String rawRefreshToken,
            RefreshTokenHash storedRefreshTokenHash
    ) {
        validateRawRefreshToken(rawRefreshToken);

        if (storedRefreshTokenHash == null) {
            throw new IllegalArgumentException(
                    "storedRefreshTokenHash cannot be null"
            );
        }

        byte[] calculatedHash =
                calculateHash(rawRefreshToken);

        byte[] storedHash;

        try {
            storedHash = HexFormat.of().parseHex(
                    storedRefreshTokenHash.getValue()
            );
        } catch (IllegalArgumentException exception) {
            return false;
        }

        return MessageDigest.isEqual(
                calculatedHash,
                storedHash
        );
    }

    private byte[] calculateHash(
            String rawRefreshToken
    ) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance(
                            HASH_ALGORITHM
                    );

            return messageDigest.digest(
                    rawRefreshToken.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 hashing is not supported",
                    exception
            );
        }
    }

    private void validateRawRefreshToken(
            String rawRefreshToken
    ) {
        if (rawRefreshToken == null
                || rawRefreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "rawRefreshToken cannot be null or blank"
            );
        }
    }
    
}
