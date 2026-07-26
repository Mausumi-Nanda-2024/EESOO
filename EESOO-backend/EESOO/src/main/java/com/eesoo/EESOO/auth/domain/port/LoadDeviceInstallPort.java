package com.eesoo.EESOO.auth.domain.port;

import java.util.Optional;
import java.util.UUID;

public interface LoadDeviceInstallPort {

    Optional<UUID> findDeviceInstallIdByInstallId(
        String installId
    );
    
}
