package com.eesoo.EESOO.user.domain.model.valueobject;

import java.util.Objects;

public final class FirstName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 30;
    private static final String LETTERS_ONLY_REGEX = "^[A-Za-z]+$";

    private final String value;

    private FirstName(String value) {
        this.value = formatName(value);
    }

    public static FirstName of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }

        String trimmed = raw.trim();

        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("First name must be at least " + MIN_LENGTH + " characters");
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("First name must not exceed " + MAX_LENGTH + " characters");
        }

        if (trimmed.contains(" ")) {
            throw new IllegalArgumentException("First name must not contain spaces");
        }

        if (!trimmed.matches(LETTERS_ONLY_REGEX)) {
            throw new IllegalArgumentException("First name must contain only alphabetic characters");
        }

        return new FirstName(trimmed);
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
        if (!(o instanceof FirstName)) return false;
        FirstName firstname = (FirstName) o;
        return Objects.equals(value, firstname.value);
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
