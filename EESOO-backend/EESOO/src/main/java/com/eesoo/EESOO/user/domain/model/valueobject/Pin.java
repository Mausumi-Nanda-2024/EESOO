package com.eesoo.EESOO.user.domain.model.valueobject;

import java.util.Objects;

import com.eesoo.EESOO.user.domain.service.PinEncoder;

public final class Pin {

    private static final int PIN_LENGTH = 4;
    private final String hashed;

    private Pin(String hashed) {
        if (hashed == null || hashed.isBlank()) {
            throw new IllegalArgumentException("PIN cannot be null or blank.");
        }
        this.hashed = hashed;
    }

    public static Pin fromRaw(String rawPin, PinEncoder encoder) {
        validateRawPin(rawPin);
        String hashed = encoder.encode(rawPin);
        return new Pin(hashed);
    }

    private static void validateRawPin(String rawPin) {
        if (rawPin == null || rawPin.isBlank()) {
            throw new IllegalArgumentException("PIN cannot be null or blank.");
        }

        if (rawPin.contains(" ")) {
            throw new IllegalArgumentException("PIN must not contain spaces.");
        }

        if (rawPin.length() != PIN_LENGTH) {
            throw new IllegalArgumentException("PIN must be exactly " + PIN_LENGTH + " digits.");
        }

        if (!rawPin.matches("\\d{" + PIN_LENGTH + "}")) {
            throw new IllegalArgumentException("PIN must contain only numeric digits.");
        }
    }

    public static Pin fromHashed(String hashedPin) {
        return new Pin(hashedPin);
    }

    public boolean isValid(String rawInput, PinEncoder encoder) {
        return encoder.matches(rawInput, this.hashed);
    }

    public String getValue() {
        return hashed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pin)) return false;
        Pin pin = (Pin) o;
        return Objects.equals(hashed, pin.hashed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashed);
    }

    @Override
    public String toString() {
        return "*".repeat(hashed.length());
    }
}
