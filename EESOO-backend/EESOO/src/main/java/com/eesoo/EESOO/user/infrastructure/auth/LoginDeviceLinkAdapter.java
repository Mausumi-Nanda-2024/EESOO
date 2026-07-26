package com.eesoo.EESOO.user.infrastructure.auth;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.domain.model.enums.LoginDeviceLinkStatus;
import com.eesoo.EESOO.auth.domain.port.LoginDeviceLinkPort;
import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceLinkFailureReason;
import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceLinkOutcome;
import com.eesoo.EESOO.user.application.device.service.DeviceLinkService;
import com.eesoo.EESOO.user.domain.model.enums.LinkedDeviceType;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;

@Component
public class LoginDeviceLinkAdapter
        implements LoginDeviceLinkPort {

    private final DeviceLinkService deviceLinkService;

    public LoginDeviceLinkAdapter(
            DeviceLinkService deviceLinkService
    ) {
        this.deviceLinkService = deviceLinkService;
    }

    @Override
    public LoginDeviceLinkStatus attemptLink(
            UUID userId,
            UUID deviceInstallId,
            String deviceId
    ) {
        Objects.requireNonNull(
                userId,
                "userId cannot be null"
        );

        Objects.requireNonNull(
                deviceInstallId,
                "deviceInstallId cannot be null"
        );

        UserId domainUserId =
                UserId.fromString(
                        userId.toString()
                );

        DeviceInstallId domainDeviceInstallId =
                DeviceInstallId.of(
                        deviceInstallId
                );

        DeviceId domainDeviceId =
                toDeviceIdOrNull(deviceId);

        DeviceLinkOutcome outcome =
                deviceLinkService
                        .tryLinkResolvedInstallation(
                                domainDeviceInstallId,
                                domainDeviceId,
                                domainUserId,
                                LinkedDeviceType.MAIN_DEVICE
                        );

        if (outcome.isLinked()) {
            return LoginDeviceLinkStatus.LINKED;
        }

        DeviceLinkFailureReason failureReason =
                outcome.getFailureReason()
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Missing device-link failure reason"
                                )
                        );

        return mapFailureReason(failureReason);
    }

    private DeviceId toDeviceIdOrNull(
            String rawDeviceId
    ) {
        if (rawDeviceId == null
                || rawDeviceId.trim().isEmpty()) {
            return null;
        }

        return DeviceId.of(
                rawDeviceId.trim()
        );
    }

    private LoginDeviceLinkStatus mapFailureReason(
            DeviceLinkFailureReason failureReason
    ) {
        return switch (failureReason) {
            case DEVICE_ID_MISSING ->
                    LoginDeviceLinkStatus
                            .DEVICE_ID_MISSING;

            case STORED_DEVICE_ID_MISSING ->
                    LoginDeviceLinkStatus
                            .STORED_DEVICE_ID_MISSING;

            case DEVICE_ID_MISMATCH ->
                    LoginDeviceLinkStatus
                            .DEVICE_ID_MISMATCH;

            case DEVICE_NOT_REGISTERED ->
                    LoginDeviceLinkStatus
                            .DEVICE_NOT_REGISTERED;

            case DEVICE_LINKED_TO_ANOTHER_USER ->
                    LoginDeviceLinkStatus
                            .DEVICE_LINKED_TO_ANOTHER_USER;

            case USER_ALREADY_HAS_ACTIVE_DEVICE ->
                    LoginDeviceLinkStatus
                            .USER_ALREADY_HAS_ACTIVE_DEVICE;
        };
    }
}