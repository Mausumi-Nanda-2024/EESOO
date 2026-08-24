package com.eesoo.EESOO.user.presentation.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceStoreRequestDTO {

    @Size(max = 100, message = "Device ID must not exceed 100 characters")
    private String deviceId;

    @NotBlank(message = "Install ID is required")
    @Size(max = 100, message = "Install ID must not exceed 100 characters")
    private String installId;

    @NotBlank(message = "OS version is required")
    @Size(max = 50, message = "OS version must not exceed 50 characters")
    private String osVersion;

    @NotBlank(message = "Platform is required")
    @Size(max = 50, message = "Platform must not exceed 50 characters")
    private String platform;

    @Size(max = 200, message = "Device ID nullable reason must not exceed 200 characters")
    private String deviceIdNullableReason;
}
