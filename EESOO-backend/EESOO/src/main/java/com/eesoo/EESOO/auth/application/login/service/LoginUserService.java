package com.eesoo.EESOO.auth.application.login.service;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.auth.application.login.command.LoginUserCommand;
import com.eesoo.EESOO.auth.application.login.dto.LoginOutcome;
import com.eesoo.EESOO.auth.application.login.dto.LoginUserDTO;
import com.eesoo.EESOO.auth.application.login.handler.LoginCommandHandler;
import com.eesoo.EESOO.auth.application.login.mapper.LoginUserMapper;

@Service
public class LoginUserService {

    private final LoginCommandHandler handler;

    public LoginUserService(
            LoginCommandHandler handler
    ) {
        this.handler = handler;
    }

    public LoginOutcome login(
            LoginUserDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "LoginUserDTO cannot be null"
            );
        }

        LoginUserCommand command =
                LoginUserMapper.toCommand(dto);

        return handler.handle(command);
    }
}
