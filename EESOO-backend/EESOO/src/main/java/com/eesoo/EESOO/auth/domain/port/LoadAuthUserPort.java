package com.eesoo.EESOO.auth.domain.port;

import java.util.Optional;

import com.eesoo.EESOO.auth.domain.model.dto.AuthUserSnapshot;

public interface LoadAuthUserPort {
    
    Optional<AuthUserSnapshot> findByPhoneNumber(String username);
}
    

