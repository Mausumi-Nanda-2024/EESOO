package com.eesoo.EESOO.auth.presentation.refresh.dto;

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
public class RefreshTokenRequestDTO {

    @NotBlank(
            message = "Refresh token is required"
    )
    @Size(max = 512, message = "Refresh token must not exceed 512 characters")
    private String refreshToken;
}
