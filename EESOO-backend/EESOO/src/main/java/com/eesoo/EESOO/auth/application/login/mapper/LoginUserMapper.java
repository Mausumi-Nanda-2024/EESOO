package com.eesoo.EESOO.auth.application.login.mapper;

import com.eesoo.EESOO.auth.application.login.command.LoginUserCommand;
import com.eesoo.EESOO.auth.application.login.dto.LoginUserDTO;
import com.eesoo.EESOO.auth.application.login.dto.LoginUserResultDTO;
import com.eesoo.EESOO.auth.domain.model.entity.AuthUser;
import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;

public class LoginUserMapper {

    public static LoginUserCommand toCommand(LoginUserDTO dto){
        return new LoginUserCommand(
                dto.getPhoneNumber(),
                dto.getPin(),
                dto.getDeviceId(),
                dto.getInstallId()
        );
    }

    public static LoginUserResultDTO toResult(
            AuthUser authUser,
            TokenPair tokenPair,
            boolean deviceLinked,
            String deviceLinkFailureReason
    ){
        return LoginUserResultDTO.from(
                authUser,
                tokenPair,
                deviceLinked,
                deviceLinkFailureReason
        );
    }
    
}
