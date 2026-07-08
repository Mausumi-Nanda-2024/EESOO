package com.eesoo.EESOO.user.presentation.register.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Pin is required")
    private String pin;


    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Install ID is required")
    private String installId;

    private String deviceId;
}
