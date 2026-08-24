package com.eesoo.EESOO.user.presentation.register.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserRequestDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    private String phoneNumber;

    @NotBlank(message = "Pin is required")
    @Pattern(
            regexp = "^\\d{4}$",
            message = "PIN must contain exactly 4 digits"
    )
    private String pin;

    @Email(message = "Invalid email")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Install ID is required")
    @Size(max = 100, message = "Install ID must not exceed 100 characters")
    private String installId;

    @Size(max = 100, message = "Device ID must not exceed 100 characters")
    private String deviceId;
}
