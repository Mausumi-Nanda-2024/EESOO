package com.eesoo.EESOO.user.application.register.service;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.user.application.register.command.RegisterUserCommand;
import com.eesoo.EESOO.user.application.register.dto.RegisterUserDTO;
import com.eesoo.EESOO.user.application.register.dto.RegisterUserResultDTO;
import com.eesoo.EESOO.user.application.register.handler.RegisterUserCommandHandler;
import com.eesoo.EESOO.user.application.register.mapper.RegisterUserMapper;

@Service
public class RegisterUserService {

    private final RegisterUserCommandHandler handler;

    public RegisterUserService(RegisterUserCommandHandler handler) {
        this.handler = handler;
    }

    public RegisterUserResultDTO register(RegisterUserDTO dto){
        RegisterUserCommand command = RegisterUserMapper.toCommand(dto);
        return handler.handle(command);
    }
}
