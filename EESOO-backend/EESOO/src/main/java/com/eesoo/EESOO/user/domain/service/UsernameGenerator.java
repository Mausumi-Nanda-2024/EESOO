package com.eesoo.EESOO.user.domain.service;

import java.security.SecureRandom;
import java.util.Locale;

import com.eesoo.EESOO.user.domain.model.valueobject.Username;

public class UsernameGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public Username generate(String firstName) {
        String base = sanitize(firstName);
        String sixDigits = String.format("%06d", RANDOM.nextInt(1_000_000));
        return Username.of(base + sixDigits);
    }

    private String sanitize(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        String sanitized = normalized.replaceAll("[^a-z0-9]", "");

        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("Username requires a valid first name");
        }

        return sanitized;
    }
}
