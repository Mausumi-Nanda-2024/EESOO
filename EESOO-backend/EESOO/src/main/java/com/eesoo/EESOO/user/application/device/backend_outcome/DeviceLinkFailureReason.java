package com.eesoo.EESOO.user.application.device.backend_outcome;

public enum DeviceLinkFailureReason {

    DEVICE_ID_MISSING,
    STORED_DEVICE_ID_MISSING,
    DEVICE_ID_MISMATCH,
    DEVICE_NOT_REGISTERED,
    DEVICE_LINKED_TO_ANOTHER_USER,
    USER_ALREADY_HAS_ACTIVE_DEVICE
}
