package com.eesoo.EESOO.auth.presentation.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserRequestDTO {

    @NotBlank(
            message = "Phone number is required"
    )
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    private String phoneNumber;

    @NotBlank(
            message = "PIN is required"
    )
    @Pattern(
            regexp = "^\\d{4}$",
            message = "PIN must contain exactly 4 digits"
    )
    private String pin;

    private String deviceId;

    @NotBlank(
            message = "Install ID is required"
    )
    private String installId;
}
