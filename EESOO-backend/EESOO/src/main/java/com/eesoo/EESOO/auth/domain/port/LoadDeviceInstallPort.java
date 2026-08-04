package com.eesoo.EESOO.auth.domain.port;

import java.util.Optional;
import java.util.UUID;

import org.springframework.modulith.NamedInterface;

@NamedInterface("user-spi")
public interface LoadDeviceInstallPort {

    Optional<UUID> findDeviceInstallIdByInstallId(
        String installId
    );
    
}
