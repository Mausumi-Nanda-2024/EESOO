package com.eesoo.EESOO.user.infrastructure.auth;

import java.util.Optional;
import java.util.UUID;

import com.eesoo.EESOO.auth.domain.port.LoadDeviceInstallPort;
import com.eesoo.EESOO.user.domain.model.valueobject.InstallId;
import com.eesoo.EESOO.user.domain.repository.DeviceInstallRepository;

public class LoadDeviceInstallAdapter implements LoadDeviceInstallPort{

    private final DeviceInstallRepository deviceInstallRepository;

     public LoadDeviceInstallAdapter(
            DeviceInstallRepository deviceInstallRepository
    ) {
        this.deviceInstallRepository =
                deviceInstallRepository;
    }

    @Override
    public Optional<UUID> findDeviceInstallIdByInstallId(
            String installId
    ) {
        if (installId == null
                || installId.trim().isEmpty()) {
            return Optional.empty();
        }

        InstallId domainInstallId =
                InstallId.of(installId);

        return deviceInstallRepository
                .findByInstallId(domainInstallId)
                .map(deviceInstall ->
                        deviceInstall
                                .getId()
                                .getValue()
                );
    }
    
    
}
