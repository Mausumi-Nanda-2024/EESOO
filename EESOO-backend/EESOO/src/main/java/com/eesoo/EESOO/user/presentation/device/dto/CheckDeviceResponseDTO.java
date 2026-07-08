package com.eesoo.EESOO.user.presentation.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckDeviceResponseDTO {

    @JsonProperty("status")
    private String status;
    @JsonProperty("failure_reason")
    private String failureReason;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("username")
    private String username;
    @JsonProperty("phone_number")
    private String phoneNumber;

 
    }
