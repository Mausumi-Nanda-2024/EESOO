package com.eesoo.EESOO.auth.domain.port;

import com.eesoo.EESOO.auth.domain.model.token.AccessTokenClaims;
import com.eesoo.EESOO.auth.domain.model.token.RefreshTokenClaims;

public interface TokenValidationPort {

     AccessTokenClaims validateAccessToken(
            String rawAccessToken
    );

    RefreshTokenClaims validateRefreshToken(
            String rawRefreshToken
    );
    
}
