package com.eesoo.EESOO.auth.application.exception;

public class DeviceInstallationNotRegisteredException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Device installation is not registered";

    public DeviceInstallationNotRegisteredException() {
        super(DEFAULT_MESSAGE);
    }
    
}
