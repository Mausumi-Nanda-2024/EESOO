package com.eesoo.EESOO.auth.presentation.pinreset.dto;

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
public class ConfirmPinResetMobileRequestDTO {

    @NotBlank(
            message = "PIN-reset attempt ID is required"
    )
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
    private String installId;
}
