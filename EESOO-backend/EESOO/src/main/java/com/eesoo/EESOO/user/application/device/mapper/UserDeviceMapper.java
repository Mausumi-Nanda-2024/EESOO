package com.eesoo.EESOO.user.application.device.mapper;

import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceRegistrationResult;
import com.eesoo.EESOO.user.application.device.command.StoreDeviceCommand;
import com.eesoo.EESOO.user.application.device.dto.DeviceCheckRequestDTO;
import com.eesoo.EESOO.user.application.device.dto.DeviceCheckResultDTO;
import com.eesoo.EESOO.user.application.device.dto.StoreDeviceRequestDTO;
import com.eesoo.EESOO.user.application.device.dto.StoreDeviceResultDTO;
import com.eesoo.EESOO.user.application.device.query.DeviceCheckQuery;
import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;
import com.eesoo.EESOO.user.presentation.device.dto.CheckDeviceRequestDTO;
import com.eesoo.EESOO.user.presentation.device.dto.CheckDeviceResponseDTO;

public class UserDeviceMapper {

    // Converts incoming client DTO -> Command for use case
    public static StoreDeviceCommand toCommand(StoreDeviceRequestDTO dto) {
        return new StoreDeviceCommand(
                dto.getDeviceId(),
                dto.getInstallId(),
                dto.getOsVersion(),
                dto.getPlatform(),
                dto.getDeviceIdNullableReason()
        );
    }

    // Converts domain Device -> result DTO for API response
    public static StoreDeviceResultDTO toResult(DeviceInstall device, DeviceRegistrationResult operationResult) {
        return StoreDeviceResultDTO.from(device, operationResult);
    }


    // Converts incoming client/application DTO -> Query for use case
    public static DeviceCheckQuery toQuery(DeviceCheckRequestDTO dto) {
        return new DeviceCheckQuery(dto.getDeviceId());
    }

    // Converts domain/use-case result -> result DTO for API response
    public static CheckDeviceResponseDTO toResult(DeviceCheckResultDTO result) {
        return new CheckDeviceResponseDTO(
                result.getStatus().name(),
                result.getFailureReason(),
                result.getUserId(),
                result.getUsername(),
                result.getPhoneNumber()
        );
    }
}
