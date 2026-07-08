package com.eesoo.EESOO.user.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eesoo.EESOO.user.infrastructure.persistence.entity.DeviceInstallJpaEntity;

public interface SpringDataDeviceInstallRepository extends JpaRepository<DeviceInstallJpaEntity, UUID> {

    boolean existsByInstallId(String installId);

    boolean existsByDeviceId(String deviceId);

    Optional<DeviceInstallJpaEntity> findByInstallId(String installId);

    Optional<DeviceInstallJpaEntity> findByDeviceId(String deviceId);
}
