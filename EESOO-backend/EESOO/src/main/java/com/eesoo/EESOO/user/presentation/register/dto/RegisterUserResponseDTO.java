package com.eesoo.EESOO.user.presentation.register.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserResponseDTO {

    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String status;
    private LocalDateTime userRegisteredAt;

    

    
}
