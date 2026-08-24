package com.eesoo.EESOO.auth.presentation.pinreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IssueResetPinRequestDTO {

    @NotBlank(
            message = "PIN-reset attempt ID is required"
    )
    @Size(max = 100, message = "PIN-reset attempt ID must not exceed 100 characters")
    private String pinResetAttemptId;

    @NotBlank(
            message = "Install ID is required"
    )
    @Size(max = 100, message = "Install ID must not exceed 100 characters")
    private String installId;
}
