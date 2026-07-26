package com.eesoo.EESOO.auth.domain.model.enums;

public enum LoginDeviceLinkStatus {
    
    LINKED,
    DEVICE_ID_MISSING,
    STORED_DEVICE_ID_MISSING,
    DEVICE_ID_MISMATCH,
    DEVICE_NOT_REGISTERED,
    DEVICE_LINKED_TO_ANOTHER_USER,
    USER_ALREADY_HAS_ACTIVE_DEVICE
}
