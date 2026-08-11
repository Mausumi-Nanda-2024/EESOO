package com.eesoo.EESOO.auth.domain.port;

import java.util.UUID;

import org.springframework.modulith.NamedInterface;

@NamedInterface("user-spi")
public interface PinVerificationPort {

boolean verifyPin(UUID userId, String pin);

}
