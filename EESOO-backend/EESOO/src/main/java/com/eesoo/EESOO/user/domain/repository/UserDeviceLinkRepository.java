package com.eesoo.EESOO.user.domain.repository;

import java.util.Optional;

import com.eesoo.EESOO.user.domain.model.entity.UserDeviceLink;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserDeviceLinkId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;

public interface UserDeviceLinkRepository {

     boolean existsById(UserDeviceLinkId id);

    Optional<UserDeviceLink> findById(UserDeviceLinkId id);

    Optional<UserDeviceLink> findActiveByDeviceInstallId(DeviceInstallId deviceInstallId);

    Optional<UserDeviceLink> findActiveByUserId(UserId userId);

    void save(UserDeviceLink userDeviceLink);

    
}
