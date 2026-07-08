package com.eesoo.EESOO.user.presentation.device.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckDeviceRequestDTO {

    @NotBlank(message = "Device ID is required for checking device link status")
    private String deviceId;


    
}
