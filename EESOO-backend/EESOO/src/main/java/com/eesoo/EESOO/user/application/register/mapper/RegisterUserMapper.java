package com.eesoo.EESOO.user.application.register.mapper;

import com.eesoo.EESOO.user.application.register.command.RegisterUserCommand;
import com.eesoo.EESOO.user.application.register.dto.RegisterUserDTO;
import com.eesoo.EESOO.user.application.register.dto.RegisterUserResultDTO;
import com.eesoo.EESOO.user.domain.model.entity.User;


public class RegisterUserMapper {

    //Converts incoming client DTO -> Command for use case
    public static RegisterUserCommand toCommand(RegisterUserDTO dto){

        return new RegisterUserCommand(
            dto.getFirstName(),
            dto.getLastName(),
            dto.getPhoneNumber(),
            dto.getPin(),
            dto.getEmail(),
            dto.getInstallId(),
            dto.getDeviceId()
        );
    }

    // Converts domain User -> result DTO for API response
    public static RegisterUserResultDTO toResult(User user){
        return RegisterUserResultDTO.from(user); // Uses the static factory method we already built
    }
    
}
