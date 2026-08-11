package com.eesoo.EESOO.auth.domain.port;

import com.eesoo.EESOO.auth.domain.model.valueobject.RefreshTokenHash;

public interface RefreshTokenHasherPort {

    RefreshTokenHash hash (
        String rawRefreshToken
    );

    boolean matches(
        String rawRefreshToken ,
        RefreshTokenHash storedRefreshTokenHash
    );
    
}
