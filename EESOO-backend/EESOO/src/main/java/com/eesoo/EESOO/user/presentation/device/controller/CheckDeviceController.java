package com.eesoo.EESOO.user.presentation.device.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.eesoo.EESOO.shared.Api.ApiResponse;
import com.eesoo.EESOO.user.application.device.dto.DeviceCheckRequestDTO;
import com.eesoo.EESOO.user.application.device.dto.DeviceCheckResultDTO;
import com.eesoo.EESOO.user.application.device.service.UserDeviceService;
import com.eesoo.EESOO.user.presentation.device.dto.CheckDeviceRequestDTO;
import com.eesoo.EESOO.user.presentation.device.dto.CheckDeviceResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/devices")
public class CheckDeviceController {

    private final UserDeviceService userDeviceService;

    // constructor injection
    public CheckDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @PostMapping(path = "/check")
    public ResponseEntity<ApiResponse<CheckDeviceResponseDTO>> check(
            @Valid @RequestBody CheckDeviceRequestDTO request
    ) {

        // convert presentation DTO to application DTO
        DeviceCheckRequestDTO appDTO = new DeviceCheckRequestDTO(
                request.getDeviceId()
        );

        DeviceCheckResultDTO result = userDeviceService.checkDevice(appDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(request.getDeviceId())
                .toUri();

        CheckDeviceResponseDTO responseDTO = new CheckDeviceResponseDTO(
                result.getStatus().name(),
                result.getFailureReason(),
                result.getUserId(),
                result.getUsername(),
                result.getPhoneNumber()
        );

        return ResponseEntity.created(location)
                .body(ApiResponse.success("Device checked successfully", responseDTO));
    }
}
