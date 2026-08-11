package com.eesoo.EESOO.user.infrastructure.auth;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.port.ResetUserPinPort;
import com.eesoo.EESOO.user.domain.model.entity.User;
import com.eesoo.EESOO.user.domain.model.valueobject.Pin;
import com.eesoo.EESOO.user.domain.repository.UserRepository;
import com.eesoo.EESOO.user.domain.service.PinEncoder;

@Component
public class ResetUserPinAdapter
        implements ResetUserPinPort {

    private final UserRepository userRepository;
    private final PinEncoder pinEncoder;

    public ResetUserPinAdapter(
            UserRepository userRepository,
            PinEncoder pinEncoder
    ) {
        this.userRepository =
                userRepository;

        this.pinEncoder =
                pinEncoder;
    }

    @Override
    public void resetPin(
            UUID userId,
            String rawNewPin
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId cannot be null"
            );
        }

        User user =
                userRepository
                        .findForUpdateById(
                                userId
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "User was not found for PIN reset"
                                )
                        );

        Pin newPin =
                Pin.fromRaw(
                        rawNewPin,
                        pinEncoder
                );

        User updatedUser =
                user.replacePin(
                        newPin
                );

        userRepository.save(
                updatedUser
        );
    }
}
