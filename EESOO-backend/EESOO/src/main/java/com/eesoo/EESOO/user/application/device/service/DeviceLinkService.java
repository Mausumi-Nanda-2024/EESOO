package com.eesoo.EESOO.user.application.device.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.shared.domain.time.TimeProvider;
import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceLinkFailureReason;
import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceLinkOutcome;
import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;
import com.eesoo.EESOO.user.domain.model.entity.UserDeviceLink;
import com.eesoo.EESOO.user.domain.model.enums.LinkedDeviceType;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.user.domain.repository.DeviceInstallRepository;
import com.eesoo.EESOO.user.domain.repository.UserDeviceLinkRepository;

@Service
public class DeviceLinkService {

    private final DeviceInstallRepository
            deviceInstallRepository;

    private final UserDeviceLinkRepository
            userDeviceLinkRepository;

    private final TimeProvider timeProvider;

    public DeviceLinkService(
            DeviceInstallRepository deviceInstallRepository,
            UserDeviceLinkRepository userDeviceLinkRepository,
            TimeProvider timeProvider
    ) {
        this.deviceInstallRepository =
                deviceInstallRepository;

        this.userDeviceLinkRepository =
                userDeviceLinkRepository;

        this.timeProvider = timeProvider;
    }

    
    @Transactional
    public DeviceLinkOutcome tryLinkByDeviceIdOnly(
            DeviceId deviceIdOrNull,
            UserId userId,
            LinkedDeviceType linkedDeviceType
    ) {
        validateUserAndDeviceType(
                userId,
                linkedDeviceType
        );

        if (deviceIdOrNull == null) {
            return DeviceLinkOutcome.notLinked(
                    DeviceLinkFailureReason.DEVICE_ID_MISSING
            );
        }

        Optional<DeviceInstall> foundDeviceInstall =
                deviceInstallRepository.findByDeviceId(
                        deviceIdOrNull
                );

        if (foundDeviceInstall.isEmpty()) {
            return DeviceLinkOutcome.notLinked(
                    DeviceLinkFailureReason.DEVICE_NOT_REGISTERED
            );
        }

        DeviceInstall deviceInstall =
                foundDeviceInstall.get();

        return createOrReuseLink(
                deviceInstall.getId(),
                userId,
                linkedDeviceType
        );
    }

    
    @Transactional
    public DeviceLinkOutcome tryLinkResolvedInstallation(
            DeviceInstallId deviceInstallId,
            DeviceId suppliedDeviceIdOrNull,
            UserId userId,
            LinkedDeviceType linkedDeviceType
    ) {
        if (deviceInstallId == null) {
            throw new IllegalArgumentException(
                    "deviceInstallId cannot be null"
            );
        }

        validateUserAndDeviceType(
                userId,
                linkedDeviceType
        );

       
        if (suppliedDeviceIdOrNull == null) {
            return DeviceLinkOutcome.notLinked(
                    DeviceLinkFailureReason.DEVICE_ID_MISSING
            );
        }

       
        Optional<DeviceInstall> foundDeviceInstall =
                deviceInstallRepository.findById(
                        deviceInstallId
                );

        if (foundDeviceInstall.isEmpty()) {
            return DeviceLinkOutcome.notLinked(
                    DeviceLinkFailureReason.DEVICE_NOT_REGISTERED
            );
        }

        DeviceInstall deviceInstall =
                foundDeviceInstall.get();

        DeviceId storedDeviceId =
                deviceInstall.getDeviceId();

        
        if (storedDeviceId == null) {
            return DeviceLinkOutcome.notLinked(
                    DeviceLinkFailureReason
                            .STORED_DEVICE_ID_MISSING
            );
        }

        
        if (!storedDeviceId.equals(
                suppliedDeviceIdOrNull
        )) {
            return DeviceLinkOutcome.notLinked(
                    DeviceLinkFailureReason
                            .DEVICE_ID_MISMATCH
            );
        }

       
        return createOrReuseLink(
                deviceInstall.getId(),
                userId,
                linkedDeviceType
        );
    }

    private DeviceLinkOutcome createOrReuseLink(
            DeviceInstallId deviceInstallId,
            UserId userId,
            LinkedDeviceType linkedDeviceType
    ) {
        Optional<UserDeviceLink> activeLinkForInstall =
                userDeviceLinkRepository
                        .findActiveByDeviceInstallId(
                                deviceInstallId
                        );

       
        if (activeLinkForInstall.isPresent()
                && !activeLinkForInstall
                        .get()
                        .getUserId()
                        .equals(userId)) {
            return DeviceLinkOutcome.notLinked(
                    DeviceLinkFailureReason
                            .DEVICE_LINKED_TO_ANOTHER_USER
            );
        }

        Optional<UserDeviceLink> activeLinkForUser =
                userDeviceLinkRepository
                        .findActiveByUserId(userId);

        if (activeLinkForUser.isPresent()) {
            UserDeviceLink existingUserLink =
                    activeLinkForUser.get();

            if (existingUserLink
                    .getDeviceInstallId()
                    .equals(deviceInstallId)) {
                return DeviceLinkOutcome.linked();
            }

           
            return DeviceLinkOutcome.notLinked(
                    DeviceLinkFailureReason
                            .USER_ALREADY_HAS_ACTIVE_DEVICE
            );
        }

      
        if (activeLinkForInstall.isPresent()) {
            return DeviceLinkOutcome.linked();
        }

        UserDeviceLink newLink =
                UserDeviceLink.createActive(
                        deviceInstallId,
                        userId,
                        linkedDeviceType,
                        timeProvider
                );

        userDeviceLinkRepository.save(newLink);

        return DeviceLinkOutcome.linked();
    }

    private void validateUserAndDeviceType(
            UserId userId,
            LinkedDeviceType linkedDeviceType
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId cannot be null"
            );
        }

        if (linkedDeviceType == null) {
            throw new IllegalArgumentException(
                    "linkedDeviceType cannot be null"
            );
        }
    }
}