package com.eesoo.EESOO.auth.domain.port;

import java.util.UUID;

import com.eesoo.EESOO.auth.domain.model.enums.LoginDeviceLinkStatus;

public interface LoginDeviceLinkPort {

    LoginDeviceLinkStatus attemptLink(
        UUID userId,
        UUID deviceInstallId,
        String deviceId
    );
    
}
