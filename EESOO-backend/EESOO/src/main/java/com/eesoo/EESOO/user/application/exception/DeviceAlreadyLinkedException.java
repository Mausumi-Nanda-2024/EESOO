package com.eesoo.EESOO.user.application.exception;

import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;

public class DeviceAlreadyLinkedException extends RuntimeException {
    public DeviceAlreadyLinkedException(DeviceId deviceId) {
        super("This device is already linked to another account. deviceId=" + deviceId);
    }
}

