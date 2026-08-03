package com.eesoo.EESOO.auth.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eesoo.EESOO.auth.infrastructure.persistence.entity.AuthSessionJpaEntity;

import jakarta.persistence.LockModeType;

public interface SpringDataAuthSessionRepository
        extends JpaRepository<
                AuthSessionJpaEntity,
                UUID
        > {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select authSession
            from AuthSessionJpaEntity authSession
            where authSession.id = :id
            """)
    Optional<AuthSessionJpaEntity>
            findForRefreshRotation(
                    @Param("id") UUID id
            );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select authSession
            from AuthSessionJpaEntity authSession
            where authSession.id = :id
            """)
    Optional<AuthSessionJpaEntity>
            findForRevocation(
                    @Param("id") UUID id
            );
}
