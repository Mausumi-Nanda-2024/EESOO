package com.eesoo.EESOO.auth.application.exception;

import java.util.Objects;

import com.eesoo.EESOO.auth.domain.model.enums.LoginDeviceLinkStatus;

public class DeviceLoginRejectedException
        extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Login rejected because device validation failed";

    private final LoginDeviceLinkStatus deviceLinkStatus;

    public DeviceLoginRejectedException(
            LoginDeviceLinkStatus deviceLinkStatus
    ) {
        super(DEFAULT_MESSAGE);

        this.deviceLinkStatus =
                Objects.requireNonNull(
                        deviceLinkStatus,
                        "deviceLinkStatus cannot be null"
                );

        if (deviceLinkStatus.allowsLogin()) {
            throw new IllegalArgumentException(
                    "An allowed device-link status cannot reject login"
            );
        }
    }

    public LoginDeviceLinkStatus getDeviceLinkStatus() {
        return deviceLinkStatus;
    }
}
