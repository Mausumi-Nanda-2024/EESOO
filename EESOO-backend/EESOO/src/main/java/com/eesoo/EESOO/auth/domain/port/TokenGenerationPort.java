package com.eesoo.EESOO.auth.domain.port;

import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;

public interface TokenGenerationPort {

    TokenPair generateTokens(UUID userId , String deviceId , String installId);
    TokenPair refreshTokens(String refreshToken , String deviceId , String installId);
    void revokeToken(String refreshToken);

    
}
