package com.eesoo.EESOO.user.infrastructure.auth;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.model.dto.AuthUserSnapshot;
import com.eesoo.EESOO.auth.domain.model.enums.LoginPermission;
import com.eesoo.EESOO.auth.domain.port.LoadAuthUserPort;
import com.eesoo.EESOO.user.domain.model.enums.UserStatus;
import com.eesoo.EESOO.user.domain.repository.UserRepository;

@Component
public class LoadAuthUserAdapter implements LoadAuthUserPort {

    private final UserRepository userRepository;

    public LoadAuthUserAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<AuthUserSnapshot> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(user -> new AuthUserSnapshot(
                        user.getId().getValue(),
                        user.getUsername().getValue(),
                        mapToLoginPermission(user.getStatus())
                ));
    }
    
    private LoginPermission mapToLoginPermission(UserStatus userStatus) {
        return switch (userStatus) {
            case ACTIVE -> LoginPermission.ELIGIBLE;
            case PENDING_VERIFICATION -> LoginPermission.PENDING_VERIFICATION;
            case DELETED -> LoginPermission.ACCOUNT_DELETED;
        };
    }

     
}
