package com.eesoo.EESOO.auth.domain.model.enums;

import org.springframework.modulith.NamedInterface;

@NamedInterface("user-spi")
public enum LoginDeviceLinkStatus {
    
    LINKED,
    DEVICE_ID_MISSING,
    STORED_DEVICE_ID_MISSING,
    DEVICE_ID_MISMATCH,

    DEVICE_NOT_REGISTERED,
    DEVICE_LINKED_TO_ANOTHER_USER,
    USER_ALREADY_HAS_ACTIVE_DEVICE,

    LINKING_FAILED;

    public boolean isLinked(){
        return this == LINKED;
    }

    public boolean allowsLogin() {
        return this == LINKED
                || this == DEVICE_ID_MISSING;
    }
}
