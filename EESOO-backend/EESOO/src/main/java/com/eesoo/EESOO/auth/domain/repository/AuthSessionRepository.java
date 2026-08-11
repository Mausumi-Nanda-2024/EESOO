package com.eesoo.EESOO.auth.domain.repository;

import java.util.Optional;

import com.eesoo.EESOO.auth.domain.model.entity.AuthSession;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;

public interface AuthSessionRepository {

    AuthSession save(AuthSession authSession);

    Optional<AuthSession> findById(AuthSessionId authSessionId);

    Optional<AuthSession> findForRefreshRotation(
            AuthSessionId authSessionId
    );

    Optional<AuthSession> findForRevocation(
            AuthSessionId authSessionId
    );
    
}
