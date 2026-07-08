package com.eesoo.EESOO.user.application.device.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceLinkFailureReason;
import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceLinkOutcome;
import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;
import com.eesoo.EESOO.user.domain.model.entity.UserDeviceLink;
import com.eesoo.EESOO.user.domain.model.enums.LinkedDeviceType;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.user.domain.repository.DeviceInstallRepository;
import com.eesoo.EESOO.user.domain.repository.UserDeviceLinkRepository;
import com.eesoo.EESOO.user.domain.service.TimeProvider;

@Service
public class DeviceLinkService {

    private final DeviceInstallRepository deviceInstallRepository;
    private final UserDeviceLinkRepository userDeviceLinkRepository;
    private final TimeProvider timeProvider;

    public DeviceLinkService(
            DeviceInstallRepository deviceInstallRepository,
            UserDeviceLinkRepository userDeviceLinkRepository,
            TimeProvider timeProvider
    ) {
        this.deviceInstallRepository = deviceInstallRepository;
        this.userDeviceLinkRepository = userDeviceLinkRepository;
        this.timeProvider = timeProvider;
    }

    @Transactional
    public DeviceLinkOutcome tryLinkByDeviceIdOnly(
            DeviceId deviceIdOrNull,
            UserId userId,
            LinkedDeviceType linkedDeviceType
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (linkedDeviceType == null) {
            throw new IllegalArgumentException("linkedDeviceType cannot be null");
        }

        if (deviceIdOrNull == null) {
            return DeviceLinkOutcome.notLinked(DeviceLinkFailureReason.DEVICE_ID_MISSING);
        }

        Optional<DeviceInstall> deviceInstallByDeviceId = deviceInstallRepository.findByDeviceId(deviceIdOrNull);
        if (deviceInstallByDeviceId.isEmpty()) {
            return DeviceLinkOutcome.notLinked(DeviceLinkFailureReason.DEVICE_NOT_REGISTERED);
        }

        DeviceInstall registeredDeviceInstall = deviceInstallByDeviceId.get();

         Optional<UserDeviceLink> activeLinkForThisInstall =
                userDeviceLinkRepository.findActiveByDeviceInstallId(registeredDeviceInstall.getId());

           if (activeLinkForThisInstall.isPresent()
                && !activeLinkForThisInstall.get().getUserId().equals(userId)) {
            return DeviceLinkOutcome.notLinked(DeviceLinkFailureReason.DEVICE_LINKED_TO_ANOTHER_USER);
        }      

        Optional<UserDeviceLink> activeLinkForThisUser =
                userDeviceLinkRepository.findActiveByUserId(userId);

        if (activeLinkForThisUser.isPresent()) {
            UserDeviceLink existingActiveLinkForUser = activeLinkForThisUser.get();

            if (existingActiveLinkForUser.getDeviceInstallId().equals(registeredDeviceInstall.getId())) {
                return DeviceLinkOutcome.linked();
            }

            return DeviceLinkOutcome.notLinked(DeviceLinkFailureReason.USER_ALREADY_HAS_ACTIVE_DEVICE);
        }

        if (activeLinkForThisInstall.isPresent()) {
            return DeviceLinkOutcome.linked();
        }



        UserDeviceLink newLink = UserDeviceLink.createActive(
                registeredDeviceInstall.getId(),
                userId,
                linkedDeviceType,
                timeProvider
        );

        userDeviceLinkRepository.save(newLink);

        return DeviceLinkOutcome.linked();
}
}
