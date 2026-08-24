package com.eesoo.EESOO.auth.presentation.pinreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPinResetMobileRequestDTO {

    @NotBlank(
            message = "PIN-reset attempt ID is required"
    )
    @Size(max = 100, message = "PIN-reset attempt ID must not exceed 100 characters")
    private String pinResetAttemptId;

    @NotBlank(
            message = "Phone number is required"
    )
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number"
    )
    private String phoneNumber;

    @NotBlank(
            message = "Install ID is required"
    )
    @Size(max = 100, message = "Install ID must not exceed 100 characters")
    private String installId;
}
