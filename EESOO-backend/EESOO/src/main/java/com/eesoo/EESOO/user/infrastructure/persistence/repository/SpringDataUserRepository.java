package com.eesoo.EESOO.user.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eesoo.EESOO.user.infrastructure.persistence.entity.UserJpaEntity;

import jakarta.persistence.LockModeType;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
	
    Optional<UserJpaEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    Optional<UserJpaEntity> findByPhoneNumber(String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT userEntity
            FROM UserJpaEntity userEntity
            WHERE userEntity.id = :userId
            """)
    Optional<UserJpaEntity> findForUpdateById(
            @Param("userId") UUID userId
    );
}
