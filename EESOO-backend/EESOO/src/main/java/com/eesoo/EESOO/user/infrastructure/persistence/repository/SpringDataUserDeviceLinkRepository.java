package com.eesoo.EESOO.user.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eesoo.EESOO.user.infrastructure.persistence.entity.UserDeviceLinkJpaEntity;

public interface SpringDataUserDeviceLinkRepository extends JpaRepository<UserDeviceLinkJpaEntity, UUID> {

    Optional<UserDeviceLinkJpaEntity> findByDeviceInstallIdAndStatus(UUID deviceInstallId, String status);

    Optional<UserDeviceLinkJpaEntity> findByUserIdAndStatus(UUID userId, String status);
}
