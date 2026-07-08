package com.eesoo.EESOO.user.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eesoo.EESOO.user.infrastructure.persistence.entity.UserJpaEntity;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
	
    Optional<UserJpaEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    Optional<UserJpaEntity> findByPhoneNumber(String phoneNumber);
}
