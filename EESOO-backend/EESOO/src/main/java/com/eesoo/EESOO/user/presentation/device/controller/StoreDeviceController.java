package com.eesoo.EESOO.user.presentation.device.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.eesoo.EESOO.shared.Api.ApiResponse;
import com.eesoo.EESOO.user.application.device.dto.StoreDeviceResultDTO;
import com.eesoo.EESOO.user.application.device.dto.StoreDeviceRequestDTO;
import com.eesoo.EESOO.user.application.device.service.UserDeviceService;
import com.eesoo.EESOO.user.presentation.device.dto.DeviceStoreRequestDTO;
import com.eesoo.EESOO.user.presentation.device.dto.DeviceStoreResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/devices")
public class StoreDeviceController {

        private final UserDeviceService userDeviceService;

        // constructor injection
        public StoreDeviceController(UserDeviceService userDeviceService) {
                this.userDeviceService = userDeviceService;
        }

        @PostMapping(path = "/store")
        public ResponseEntity<ApiResponse<DeviceStoreResponseDTO>> register(
                        @Valid @RequestBody DeviceStoreRequestDTO request) {

                // convert presentation DTO to application DTO
                StoreDeviceRequestDTO appDTO = new StoreDeviceRequestDTO(
                                request.getDeviceId(),
                                request.getInstallId(),
                                request.getOsVersion(),
                                request.getPlatform(),
                                request.getDeviceIdNullableReason());

                StoreDeviceResultDTO result = userDeviceService.storeDevice(appDTO);

                URI location = ServletUriComponentsBuilder
                                .fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(result.getInstallId())
                                .toUri();

                DeviceStoreResponseDTO responseDTO = new DeviceStoreResponseDTO(
                                result.getDeviceId(),
                                result.getInstallId(),
                                result.getOsVersion(),
                                result.getPlatform(),
                                result.getDeviceIdNullableReason(),
                                 result.getOperationResult());

                return ResponseEntity.created(location)
                                .body(ApiResponse.success("Device Stored successfully", responseDTO));

        }

}
