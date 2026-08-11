package com.eesoo.EESOO.auth.domain.port;

import java.util.UUID;

import org.springframework.modulith.NamedInterface;

@NamedInterface("user-spi")
public interface ResetUserPinPort {

    void resetPin(
            UUID userId,
            String rawNewPin
    );
}
