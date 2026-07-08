package com.eesoo.EESOO.auth.domain.port;

import java.util.UUID;

public interface PinVerificationPort {

boolean verifyPin(UUID userId, String pin);

}
