package com.eesoo.EESOO.auth.domain.port;

import java.util.Optional;

import org.springframework.modulith.NamedInterface;

import com.eesoo.EESOO.auth.domain.model.dto.AuthUserSnapshot;

@NamedInterface("user-spi")
public interface LoadAuthUserPort {
    
    Optional<AuthUserSnapshot> findByPhoneNumber(String phoneNumber);
}
    

