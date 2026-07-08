package com.eesoo.EESOO.user.presentation.device.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceStoreRequestDTO {

    private String deviceId;

    @NotBlank(message = "Install ID is required")
    private String installId;

    @NotBlank(message = "OS version is required")
    private String osVersion;

    @NotBlank(message = "Platform is required")
    private String platform;

    private String deviceIdNullableReason;
}
