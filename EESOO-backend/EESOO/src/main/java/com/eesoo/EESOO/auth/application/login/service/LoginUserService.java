package com.eesoo.EESOO.auth.application.login.service;

import java.util.Optional;

import com.eesoo.EESOO.auth.application.login.command.LoginUserCommand;
import com.eesoo.EESOO.auth.application.login.dto.LoginUserDTO;
import com.eesoo.EESOO.auth.application.login.handler.LoginCommandHandler;
import com.eesoo.EESOO.auth.application.login.mapper.LoginUserMapper;
import com.eesoo.EESOO.auth.domain.model.dto.AuthUserSnapshot;
import com.eesoo.EESOO.auth.domain.model.entity.AuthUser;

public class LoginUserService {

    private final LoginCommandHandler handler;

    public LoginUserService(LoginCommandHandler handler) {
        this.handler = handler;
    }

    public Optional<AuthUser> login(LoginUserDTO dto) {
        LoginUserCommand command = LoginUserMapper.toCommand(dto);
        return handler.handle(command);
    }

}
