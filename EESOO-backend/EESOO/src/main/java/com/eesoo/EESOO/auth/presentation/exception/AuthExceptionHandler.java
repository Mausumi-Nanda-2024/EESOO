package com.eesoo.EESOO.auth.presentation.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eesoo.EESOO.auth.application.exception.AuthSessionNotUsableException;
import com.eesoo.EESOO.auth.application.exception.DeviceInstallationNotRegisteredException;
import com.eesoo.EESOO.auth.application.exception.DeviceLoginRejectedException;
import com.eesoo.EESOO.auth.application.exception.InvalidCredentialsException;
import com.eesoo.EESOO.auth.application.exception.RefreshTokenReplayDetectedException;
import com.eesoo.EESOO.auth.domain.exception.InvalidTokenException;
import com.eesoo.EESOO.shared.Api.ApiResponse;

@RestControllerAdvice(
        basePackages = "com.eesoo.EESOO.auth"
)
public class AuthExceptionHandler {

    @ExceptionHandler(
            InvalidCredentialsException.class
    )
    public ResponseEntity<ApiResponse<Object>>
            handleInvalidCredentials(
                    InvalidCredentialsException exception
            ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.error(
                                exception.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(
            DeviceInstallationNotRegisteredException.class
    )
    public ResponseEntity<ApiResponse<Object>>
            handleDeviceInstallationNotRegistered(
                    DeviceInstallationNotRegisteredException exception
            ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ApiResponse.error(
                                exception.getMessage(),
                                Map.of(
                                        "code",
                                        "DEVICE_NOT_REGISTERED"
                                )
                        )
                );
    }

    @ExceptionHandler(
            DeviceLoginRejectedException.class
    )
    public ResponseEntity<ApiResponse<Object>>
            handleDeviceLoginRejected(
                    DeviceLoginRejectedException exception
            ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        ApiResponse.error(
                                exception.getMessage(),
                                Map.of(
                                        "code",
                                        exception
                                                .getDeviceLinkStatus()
                                                .name()
                                )
                        )
                );
    }

    @ExceptionHandler(
            InvalidTokenException.class
    )
    public ResponseEntity<ApiResponse<Object>>
            handleInvalidToken(
                    InvalidTokenException exception
            ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.error(
                                exception.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(
            AuthSessionNotUsableException.class
    )
    public ResponseEntity<ApiResponse<Object>>
            handleAuthSessionNotUsable(
                    AuthSessionNotUsableException exception
            ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.error(
                                exception.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(
            RefreshTokenReplayDetectedException.class
    )
    public ResponseEntity<ApiResponse<Object>>
            handleRefreshTokenReplayDetected(
                    RefreshTokenReplayDetectedException exception
            ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.error(
                                exception.getMessage(),
                                Map.of(
                                        "code",
                                        "REFRESH_TOKEN_REUSE_DETECTED"
                                )
                        )
                );
    }
}
