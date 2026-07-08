package com.eesoo.EESOO.user.application.exception;

import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(DeviceId deviceId) {
        super("Device not found. App install step must run before registration. deviceId=" + deviceId);
    }
}

