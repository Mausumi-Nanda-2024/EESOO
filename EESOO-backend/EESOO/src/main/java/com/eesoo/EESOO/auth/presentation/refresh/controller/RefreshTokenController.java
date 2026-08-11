package com.eesoo.EESOO.auth.presentation.refresh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eesoo.EESOO.auth.application.refresh.dto.RefreshTokenDTO;
import com.eesoo.EESOO.auth.application.refresh.dto.RefreshTokenResultDTO;
import com.eesoo.EESOO.auth.application.refresh.service.RefreshTokenService;
import com.eesoo.EESOO.auth.presentation.refresh.dto.RefreshTokenRequestDTO;
import com.eesoo.EESOO.auth.presentation.refresh.dto.RefreshTokenResponseDTO;
import com.eesoo.EESOO.shared.Api.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenController(
            RefreshTokenService refreshTokenService
    ) {
        this.refreshTokenService =
                refreshTokenService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<
            ApiResponse<RefreshTokenResponseDTO>
    > refresh(
            @Valid
            @RequestBody
            RefreshTokenRequestDTO request
    ) {
        RefreshTokenDTO applicationDTO =
                new RefreshTokenDTO(
                        request.getRefreshToken()
                );

        RefreshTokenResultDTO result =
                refreshTokenService.refresh(
                        applicationDTO
                );

        RefreshTokenResponseDTO response =
                RefreshTokenResponseDTO.from(
                        result
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tokens refreshed successfully",
                        response
                )
        );
    }
}
