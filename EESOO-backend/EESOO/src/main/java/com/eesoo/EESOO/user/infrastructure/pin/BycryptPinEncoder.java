package com.eesoo.EESOO.user.infrastructure.pin;

import com.eesoo.EESOO.user.domain.service.PinEncoder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class BycryptPinEncoder implements PinEncoder {

    @Override
    public String encode(String rawPin) {
        return BCrypt.hashpw(rawPin, BCrypt.gensalt());
    }

    @Override
    public boolean matches(String rawPin, String hashedPin) {
        return BCrypt.checkpw(rawPin, hashedPin);
    }

    
    
}
