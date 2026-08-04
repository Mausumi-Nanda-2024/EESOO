package com.eesoo.EESOO.auth.domain.port;

import java.util.UUID;

import org.springframework.modulith.NamedInterface;

import com.eesoo.EESOO.auth.domain.model.enums.LoginDeviceLinkStatus;

@NamedInterface("user-spi")
public interface LoginDeviceLinkPort {

    LoginDeviceLinkStatus attemptLink(
        UUID userId,
        UUID deviceInstallId,
        String deviceId
    );
    
}
