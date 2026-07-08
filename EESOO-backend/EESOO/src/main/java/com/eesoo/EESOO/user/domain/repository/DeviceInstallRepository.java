package com.eesoo.EESOO.user.domain.repository;

import java.util.Optional;

import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.InstallId;

public interface DeviceInstallRepository {

    boolean existsById(DeviceInstallId id);

    boolean existsByInstallId(InstallId installId);

    boolean existsByDeviceId(DeviceId deviceId);

    Optional<DeviceInstall> findById(DeviceInstallId id);

    Optional<DeviceInstall> findByInstallId(InstallId installId);

    Optional<DeviceInstall> findByDeviceId(DeviceId deviceId);

    void save(DeviceInstall deviceInstall);
    
}
