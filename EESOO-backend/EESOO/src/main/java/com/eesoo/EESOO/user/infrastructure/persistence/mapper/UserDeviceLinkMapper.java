package com.eesoo.EESOO.user.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.user.domain.model.entity.UserDeviceLink;
import com.eesoo.EESOO.user.domain.model.enums.LinkStatus;
import com.eesoo.EESOO.user.domain.model.enums.LinkedDeviceType;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserDeviceLinkId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.user.infrastructure.persistence.entity.UserDeviceLinkJpaEntity;

@Component
public class UserDeviceLinkMapper {

    public UserDeviceLinkJpaEntity toEntity(UserDeviceLink userDeviceLink) {
        return new UserDeviceLinkJpaEntity(
                userDeviceLink.getId().getValue(),
                userDeviceLink.getDeviceInstallId().getValue(),
                userDeviceLink.getUserId().getValue(),
                userDeviceLink.getStatus().name(),
                userDeviceLink.getLinkedDeviceType().name(),
                userDeviceLink.getLinkedAt(),
                userDeviceLink.getUnlinkedAt()
        );
    }

    public UserDeviceLink toDomain(UserDeviceLinkJpaEntity entity) {
        return UserDeviceLink.rehydrate(
                UserDeviceLinkId.of(entity.getId()),
                DeviceInstallId.of(entity.getDeviceInstallId()),
                UserId.fromString(entity.getUserId().toString()),
                LinkStatus.valueOf(entity.getStatus()),
                LinkedDeviceType.valueOf(entity.getLinkedDeviceType()),
                entity.getLinkedAt(),
                entity.getUnlinkedAt()
        );
    }
}
