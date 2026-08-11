package com.eesoo.EESOO.auth.application.pinreset.service;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.auth.application.pinreset.command.IssueResetPinCommand;
import com.eesoo.EESOO.auth.application.pinreset.dto.IssueResetPinDTO;
import com.eesoo.EESOO.auth.application.pinreset.dto.IssueResetPinResultDTO;
import com.eesoo.EESOO.auth.application.pinreset.handler.IssueResetPinCommandHandler;
import com.eesoo.EESOO.auth.application.pinreset.mapper.IssueResetPinMapper;

@Service
public class IssueResetPinService {

    private final IssueResetPinCommandHandler handler;

    public IssueResetPinService(
            IssueResetPinCommandHandler handler
    ) {
        this.handler = handler;
    }

    public IssueResetPinResultDTO issuePin(
            IssueResetPinDTO dto
    ) {
        IssueResetPinCommand command =
                IssueResetPinMapper.toCommand(
                        dto
                );

        return handler.handle(
                command
        );
    }
}
