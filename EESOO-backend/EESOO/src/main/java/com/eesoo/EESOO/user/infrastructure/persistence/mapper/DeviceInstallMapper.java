package com.eesoo.EESOO.user.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;
import com.eesoo.EESOO.user.domain.model.enums.DeviceIdNullableReason;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.InstallId;
import com.eesoo.EESOO.user.infrastructure.persistence.entity.DeviceInstallJpaEntity;

@Component
public class DeviceInstallMapper {

    public DeviceInstallJpaEntity toEntity(DeviceInstall deviceInstall) {
        return new DeviceInstallJpaEntity(
                deviceInstall.getId().getValue(),
                deviceInstall.getDeviceId() != null ? deviceInstall.getDeviceId().getValue() : null,
                deviceInstall.getInstallId().getValue(),
                deviceInstall.getOsVersion(),
                deviceInstall.getPlatform(),
                deviceInstall.getDeviceIdNullableReason() != null
                        ? deviceInstall.getDeviceIdNullableReason().name()
                        : null,
                deviceInstall.getFirstSeenAt(),
                deviceInstall.getLastSeenAt()
        );
    }

    public DeviceInstall toDomain(DeviceInstallJpaEntity entity) {
        return DeviceInstall.rehydrate(
                DeviceInstallId.of(entity.getId()),
                entity.getDeviceId() != null ? DeviceId.of(entity.getDeviceId()) : null,
                InstallId.of(entity.getInstallId()),
                entity.getOsVersion(),
                entity.getPlatform(),
                entity.getDeviceIdNullableReason() != null
                        ? DeviceIdNullableReason.valueOf(entity.getDeviceIdNullableReason())
                        : null,
                entity.getFirstSeenAt(),
                entity.getLastSeenAt()
        );
    }
}
