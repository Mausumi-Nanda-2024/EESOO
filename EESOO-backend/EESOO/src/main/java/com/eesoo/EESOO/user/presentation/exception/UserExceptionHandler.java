package com.eesoo.EESOO.user.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.eesoo.EESOO.shared.Api.ApiResponse;
import com.eesoo.EESOO.user.application.exception.DeviceAlreadyLinkedException;
import com.eesoo.EESOO.user.application.exception.DeviceNotFoundException;
import com.eesoo.EESOO.user.application.exception.EmailAlreadyExistsException;
import com.eesoo.EESOO.user.application.exception.PhoneNumberAlreadyExistsException;
import com.eesoo.EESOO.user.application.exception.UsernameGenerationFailedException;

@RestControllerAdvice(basePackages = "com.eesoo.EESOO.user")
public class UserExceptionHandler {

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handlePhoneNumberExists(PhoneNumberAlreadyExistsException ex) {

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailExists(EmailAlreadyExistsException ex) {

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(UsernameGenerationFailedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameGenerationFailure(
            UsernameGenerationFailedException ex) {

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleDeviceNotFound(DeviceNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(DeviceAlreadyLinkedException.class)
    public ResponseEntity<ApiResponse<Object>> handleDeviceAlreadyLinked(DeviceAlreadyLinkedException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}
