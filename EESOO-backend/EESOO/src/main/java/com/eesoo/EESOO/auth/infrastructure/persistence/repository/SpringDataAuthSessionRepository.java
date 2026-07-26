package com.eesoo.EESOO.auth.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eesoo.EESOO.auth.infrastructure.persistence.entity.AuthSessionJpaEntity;

public interface SpringDataAuthSessionRepository
        extends JpaRepository<
                AuthSessionJpaEntity,
                UUID
        > {
}