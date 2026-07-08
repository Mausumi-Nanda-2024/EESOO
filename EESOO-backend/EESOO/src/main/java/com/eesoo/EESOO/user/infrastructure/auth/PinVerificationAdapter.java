package com.eesoo.EESOO.user.infrastructure.auth;

import java.util.Optional;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.port.PinVerificationPort;
import com.eesoo.EESOO.user.domain.model.entity.User;
import com.eesoo.EESOO.user.domain.repository.UserRepository;
import com.eesoo.EESOO.user.domain.service.PinEncoder;

public class PinVerificationAdapter implements PinVerificationPort {

    private final UserRepository userRepository;
    private final PinEncoder pinEncoder;

    public PinVerificationAdapter(UserRepository userRepository, PinEncoder pinEncoder) {
        this.userRepository = userRepository;
        this.pinEncoder = pinEncoder;
    }

    @Override
    public boolean verifyPin(UUID userId, String pin) {
        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) {
            return false;
        }
                            
        User user = foundUser.get();
        return pinEncoder.matches(pin, user.getPin().getValue());

    }

}
