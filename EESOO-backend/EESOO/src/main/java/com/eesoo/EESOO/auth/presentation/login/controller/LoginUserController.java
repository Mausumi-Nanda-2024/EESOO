package com.eesoo.EESOO.auth.presentation.login.controller;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eesoo.EESOO.auth.application.login.dto.LoginOutcome;
import com.eesoo.EESOO.auth.application.login.dto.LoginUserDTO;
import com.eesoo.EESOO.auth.application.login.dto.LoginUserResultDTO;
import com.eesoo.EESOO.auth.application.login.dto.PinLoginFailureResultDTO;
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
            ApiResponse<Object>
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

        LoginOutcome outcome =
                loginUserService.login(
                        applicationDTO
                );

        if (!outcome.isSuccessful()) {
            return pinFailureResponse(
                    outcome.getPinFailure()
            );
        }

        LoginUserResultDTO result =
                outcome.getSuccessfulLogin();

        LoginUserResponseDTO response =
                LoginUserResponseDTO.from(
                        result
                );

        return ResponseEntity.ok(
                ApiResponse.<Object>success(
                        "Login successful",
                        response
                )
        );
    }

    private static ResponseEntity<ApiResponse<Object>>
            pinFailureResponse(
                    PinLoginFailureResultDTO failure
            ) {
        LinkedHashMap<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "code",
                failure.getCode()
        );

        data.put(
                "resetStatus",
                failure.getResetStatus().name()
        );

        data.put(
                "remainingAttempts",
                failure.getRemainingAttempts()
        );

        if (!failure
                .getResetStatus()
                .isFirstFailure()) {
            data.put(
                    "pinResetAttemptId",
                    failure.getPinResetAttemptId()
            );
        }

        HttpStatus responseStatus =
                failure
                        .getResetStatus()
                        .isFirstFailure()
                                ? HttpStatus.UNAUTHORIZED
                                : HttpStatus.LOCKED;

        return ResponseEntity
                .status(responseStatus)
                .body(
                        ApiResponse.<Object>error(
                                failure.getMessage(),
                                data
                        )
                );
    }
}
