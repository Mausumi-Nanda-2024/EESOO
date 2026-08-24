package com.eesoo.EESOO.user.presentation.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckDeviceRequestDTO {

    @NotBlank(message = "Device ID is required for checking device link status")
    @Size(max = 100, message = "Device ID must not exceed 100 characters")
    private String deviceId;
}
