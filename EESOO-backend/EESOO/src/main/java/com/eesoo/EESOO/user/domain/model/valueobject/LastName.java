package com.eesoo.EESOO.user.domain.model.valueobject;

import java.util.Objects;

public final class LastName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 30;
    private static final String LETTERS_ONLY_REGEX = "^[A-Za-z]+$";

    private final String value;

    private LastName(String value) {
        this.value = formatName(value);
    }

    public static LastName of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }

        String trimmed = raw.trim();

        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Last name must be at least " + MIN_LENGTH + " characters");
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Last name must not exceed " + MAX_LENGTH + " characters");
        }

        if (trimmed.contains(" ")) {
            throw new IllegalArgumentException("Last name must not contain spaces");
        }

        if (!trimmed.matches(LETTERS_ONLY_REGEX)) {
            throw new IllegalArgumentException("Last name must contain only alphabetic characters");
        }

        return new LastName(trimmed);
    }

    private String formatName(String input) {
        String lower = input.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LastName)) return false;
        LastName lastname = (LastName) o;
        return Objects.equals(value, lastname.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
