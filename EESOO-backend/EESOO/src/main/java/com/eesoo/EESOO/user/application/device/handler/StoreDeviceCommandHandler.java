package com.eesoo.EESOO.user.application.device.handler;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.shared.application.cqrs.CommandHandler;
import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceRegistrationResult;
import com.eesoo.EESOO.user.application.device.command.StoreDeviceCommand;
import com.eesoo.EESOO.user.application.device.dto.StoreDeviceResultDTO;
import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;
import com.eesoo.EESOO.user.domain.model.enums.DeviceIdNullableReason;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.InstallId;
import com.eesoo.EESOO.user.domain.repository.DeviceInstallRepository;
import com.eesoo.EESOO.user.domain.service.TimeProvider;

@Service
public class StoreDeviceCommandHandler implements CommandHandler<StoreDeviceCommand, StoreDeviceResultDTO> {

    private final DeviceInstallRepository deviceInstallRepository;
    private final TimeProvider timeProvider;

    public StoreDeviceCommandHandler(DeviceInstallRepository deviceInstallRepository, TimeProvider timeProvider) {
        this.deviceInstallRepository = deviceInstallRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    public StoreDeviceResultDTO handle(StoreDeviceCommand command) {
        DeviceId deviceId = toDeviceIdOrNull(command.getDeviceId());
        InstallId installId = InstallId.of(command.getInstallId());
        DeviceIdNullableReason nullableReason = toNullableReasonOrNull(command.getDeviceIdNullableReason());

        if (deviceId != null) {
            Optional<DeviceInstall> byDeviceIdOptional = deviceInstallRepository.findByDeviceId(deviceId);

            if (byDeviceIdOptional.isPresent()) {
                DeviceInstall existingByDevice = byDeviceIdOptional.get();

                if (!existingByDevice.getInstallId().equals(installId)) {
                    DeviceInstall updated = existingByDevice
                            .withNewInstallId(installId)
                            .touch(
                                    command.getOsVersion(),
                                    command.getPlatform(),
                                    nullableReason,
                                    timeProvider);

                    deviceInstallRepository.save(updated);
                    return StoreDeviceResultDTO.from(updated, DeviceRegistrationResult.ALREADY_STORED);
                }

                DeviceInstall updated = existingByDevice.touch(
                        command.getOsVersion(),
                        command.getPlatform(),
                        nullableReason,
                        timeProvider);

                deviceInstallRepository.save(updated);
                return StoreDeviceResultDTO.from(updated, DeviceRegistrationResult.ALREADY_STORED);
            }
        }

        Optional<DeviceInstall> existingByInstallIdOptional = deviceInstallRepository.findByInstallId(installId);
        if (existingByInstallIdOptional.isPresent()) {
            DeviceInstall existingByInstallId = existingByInstallIdOptional.get();

            DeviceInstall updated = existingByInstallId.touch(
                    command.getOsVersion(),
                    command.getPlatform(),
                    nullableReason,
                    timeProvider);

            // Progressive Device ID Attachment Logic
            if (deviceId != null && existingByInstallId.getDeviceId() == null) {
                updated = updated.attachDeviceId(deviceId);
                deviceInstallRepository.save(updated);
                return StoreDeviceResultDTO.from(updated, DeviceRegistrationResult.STORED);
            }

            // Device ID Failed to Store Logic
            if (deviceId == null && nullableReason != null) {
                deviceInstallRepository.save(updated);
                return StoreDeviceResultDTO.from(updated, DeviceRegistrationResult.FAILED_TO_STORE);
            }

            deviceInstallRepository.save(updated);
            return StoreDeviceResultDTO.from(updated, DeviceRegistrationResult.ALREADY_STORED);
        }

        DeviceInstall newInstall = DeviceInstall.initializeOnFirstSeen(
                deviceId,
                installId,
                command.getOsVersion(),
                command.getPlatform(),
                nullableReason,
                timeProvider);
        deviceInstallRepository.save(newInstall);

        // Check if deviceId failed to store during new install creation
        if (deviceId == null && nullableReason != null) {
            return StoreDeviceResultDTO.from(newInstall, DeviceRegistrationResult.FAILED_TO_STORE);
        }

        return StoreDeviceResultDTO.from(newInstall, DeviceRegistrationResult.STORED);
    }

    private DeviceId toDeviceIdOrNull(String rawDeviceId) {
        if (rawDeviceId == null || rawDeviceId.trim().isEmpty()) {
            return null;
        }
        return DeviceId.of(rawDeviceId);
    }

    private DeviceIdNullableReason toNullableReasonOrNull(String rawReason) {
        if (rawReason == null || rawReason.trim().isEmpty()) {
            return null;
        }
        return DeviceIdNullableReason.valueOf(rawReason.trim().toUpperCase());
    }
}
