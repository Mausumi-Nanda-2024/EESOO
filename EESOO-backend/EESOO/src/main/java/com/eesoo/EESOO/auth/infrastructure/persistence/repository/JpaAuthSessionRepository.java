package com.eesoo.EESOO.auth.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.eesoo.EESOO.auth.domain.model.entity.AuthSession;
import com.eesoo.EESOO.auth.domain.model.valueobject.AuthSessionId;
import com.eesoo.EESOO.auth.domain.repository.AuthSessionRepository;
import com.eesoo.EESOO.auth.infrastructure.persistence.entity.AuthSessionJpaEntity;
import com.eesoo.EESOO.auth.infrastructure.persistence.mapper.AuthSessionMapper;

@Repository
public class JpaAuthSessionRepository implements AuthSessionRepository{

    private final SpringDataAuthSessionRepository springRepo;
    private final AuthSessionMapper mapper;

    public JpaAuthSessionRepository(
            SpringDataAuthSessionRepository springRepository,
            AuthSessionMapper mapper
    ) {
        this.springRepo = springRepository;
        this.mapper = mapper;
    }

    @Override
    public AuthSession save(
            AuthSession authSession
    ) {
        AuthSessionJpaEntity entity =
                mapper.toEntity(authSession);

        AuthSessionJpaEntity savedEntity =
                springRepo.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AuthSession> findById(
            AuthSessionId authSessionId
    ) {
        return springRepo
                .findById(authSessionId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<AuthSession> findForRefreshRotation(
            AuthSessionId authSessionId
    ) {
        return springRepo
                .findForRefreshRotation(
                        authSessionId.getValue()
                )
                .map(mapper::toDomain);
    }

    @Override
    public Optional<AuthSession> findForRevocation(
            AuthSessionId authSessionId
    ) {
        return springRepo
                .findForRevocation(
                        authSessionId.getValue()
                )
                .map(mapper::toDomain);
    }
    
}
