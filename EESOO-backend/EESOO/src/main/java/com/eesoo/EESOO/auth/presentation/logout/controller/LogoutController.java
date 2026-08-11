package com.eesoo.EESOO.auth.presentation.logout.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eesoo.EESOO.auth.application.logout.dto.LogoutDTO;
import com.eesoo.EESOO.auth.application.logout.service.LogoutService;
import com.eesoo.EESOO.auth.infrastructure.security.AuthenticatedUserPrincipal;
import com.eesoo.EESOO.shared.Api.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {

    private final LogoutService logoutService;

    public LogoutController(
            LogoutService logoutService
    ) {
        this.logoutService = logoutService;
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
            @AuthenticationPrincipal
            AuthenticatedUserPrincipal principal
    ) {
        LogoutDTO applicationDTO =
                new LogoutDTO(
                        principal.getUserId(),
                        principal.getSessionId()
                );

        logoutService.logout(
                applicationDTO
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logout successful",
                        null
                )
        );
    }
}
