package com.eesoo.EESOO.auth.presentation.pinreset.dto;

import jakarta.validation.constraints.NotBlank;
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
    private String pinResetAttemptId;

    @NotBlank(
            message = "Install ID is required"
    )
    private String installId;
}
