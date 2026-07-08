package com.eesoo.EESOO.user.presentation.device.dto;

import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceRegistrationResult;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class DeviceStoreResponseDTO {
    
    private String deviceId;
    private String installId;
    private String osVersion;
    private String platform;
    private String deviceIdNullableReason;
    private DeviceRegistrationResult operationResult;
}
