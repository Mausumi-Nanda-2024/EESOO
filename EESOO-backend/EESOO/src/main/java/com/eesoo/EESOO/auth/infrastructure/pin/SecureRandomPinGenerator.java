package com.eesoo.EESOO.auth.infrastructure.pin;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.service.PinGenerator;

@Component
public class SecureRandomPinGenerator
        implements PinGenerator {

    private static final int PIN_LIMIT = 10_000;

    private final SecureRandom secureRandom;

    public SecureRandomPinGenerator() {
        this.secureRandom =
                new SecureRandom();
    }

    @Override
    public String generateFourDigitPin() {
        int generatedNumber =
                secureRandom.nextInt(
                        PIN_LIMIT
                );

        return String.format(
                "%04d",
                generatedNumber
        );
    }
}
