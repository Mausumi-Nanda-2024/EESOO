package com.eesoo.EESOO.user.presentation.register.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.eesoo.EESOO.shared.Api.ApiResponse;
import com.eesoo.EESOO.user.application.register.dto.RegisterUserDTO;
import com.eesoo.EESOO.user.application.register.dto.RegisterUserResultDTO;
import com.eesoo.EESOO.user.application.register.service.RegisterUserService;
import com.eesoo.EESOO.user.presentation.register.dto.RegisterUserRequestDTO;
import com.eesoo.EESOO.user.presentation.register.dto.RegisterUserResponseDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class RegisterUserController {

    private final RegisterUserService registerUserService;

    //constructor injection
    public RegisterUserController(RegisterUserService registerUserService) {
        this.registerUserService = registerUserService;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<ApiResponse<RegisterUserResponseDTO>> register(@Valid @RequestBody RegisterUserRequestDTO request ){

    // convert presentation DTO to application DTO
    RegisterUserDTO appDTO = new RegisterUserDTO(
        request.getFirstName(), 
        request.getLastName(), 
        request.getPhoneNumber(), 
        request.getPin(), 
        request.getEmail(),
        request.getInstallId(),
        request.getDeviceId()
    );

    RegisterUserResultDTO result = registerUserService.register(appDTO);

    URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(result.getUserId())
            .toUri();

    RegisterUserResponseDTO responseDTO = new RegisterUserResponseDTO(result.getUserId(), result.getUsername(), result.getFirstName(), result.getLastName(), result.getPhoneNumber(), result.getEmail(), result.getStatus(), result.getUserRegisteredAt());

    return ResponseEntity.created(location)
            .body(ApiResponse.success("User registered successfully", responseDTO));


    }
}
