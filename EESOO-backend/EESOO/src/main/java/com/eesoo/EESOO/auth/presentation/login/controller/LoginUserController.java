package com.eesoo.EESOO.auth.presentation.login.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eesoo.EESOO.auth.application.login.dto.LoginUserDTO;
import com.eesoo.EESOO.auth.application.login.dto.LoginUserResultDTO;
import com.eesoo.EESOO.auth.application.login.service.LoginUserService;
import com.eesoo.EESOO.auth.presentation.login.dto.LoginUserRequestDTO;
import com.eesoo.EESOO.auth.presentation.login.dto.LoginUserResponseDTO;
import com.eesoo.EESOO.shared.Api.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginUserController {

    private final LoginUserService loginUserService;

    public LoginUserController(
            LoginUserService loginUserService
    ) {
        this.loginUserService = loginUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<
            ApiResponse<LoginUserResponseDTO>
    > login(
            @Valid
            @RequestBody
            LoginUserRequestDTO request
    ) {
        LoginUserDTO applicationDTO =
                new LoginUserDTO(
                        request.getPhoneNumber(),
                        request.getPin(),
                        request.getDeviceId(),
                        request.getInstallId()
                );

        LoginUserResultDTO result =
                loginUserService.login(
                        applicationDTO
                );

        LoginUserResponseDTO response =
                LoginUserResponseDTO.from(
                        result
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        response
                )
        );
    }
}
