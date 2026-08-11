package com.eesoo.EESOO.auth.application.pinreset.handler;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.auth.application.exception.PinResetAttemptNotFoundException;
import com.eesoo.EESOO.auth.application.exception.PinResetMobileVerificationException;
import com.eesoo.EESOO.auth.application.pinreset.command.ConfirmPinResetMobileCommand;
import com.eesoo.EESOO.auth.application.pinreset.dto.ConfirmPinResetMobileResultDTO;
import com.eesoo.EESOO.auth.application.pinreset.mapper.ConfirmPinResetMobileMapper;
import com.eesoo.EESOO.auth.domain.model.dto.AuthUserSnapshot;
import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptId;
import com.eesoo.EESOO.auth.domain.port.LoadAuthUserPort;
import com.eesoo.EESOO.auth.domain.port.LoadDeviceInstallPort;
import com.eesoo.EESOO.auth.domain.repository.PinResetAttemptRepository;
import com.eesoo.EESOO.shared.application.cqrs.CommandHandler;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;

@Component
public class ConfirmPinResetMobileCommandHandler
        implements CommandHandler<
                ConfirmPinResetMobileCommand,
                ConfirmPinResetMobileResultDTO
        > {

    private final PinResetAttemptRepository pinResetAttemptRepository;
    private final LoadAuthUserPort loadAuthUserPort;
    private final LoadDeviceInstallPort loadDeviceInstallPort;
    private final TimeProvider timeProvider;

    public ConfirmPinResetMobileCommandHandler(
            PinResetAttemptRepository pinResetAttemptRepository,
            LoadAuthUserPort loadAuthUserPort,
            LoadDeviceInstallPort loadDeviceInstallPort,
            TimeProvider timeProvider
    ) {
        this.pinResetAttemptRepository =
                pinResetAttemptRepository;

        this.loadAuthUserPort =
                loadAuthUserPort;

        this.loadDeviceInstallPort =
                loadDeviceInstallPort;

        this.timeProvider =
                timeProvider;
    }

    @Override
    @Transactional
    public ConfirmPinResetMobileResultDTO handle(
            ConfirmPinResetMobileCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "ConfirmPinResetMobileCommand cannot be null"
            );
        }

        PinResetAttemptId attemptId =
                parseAttemptId(
                        command.getPinResetAttemptId()
                );

        PinResetAttempt attempt =
                pinResetAttemptRepository
                        .findForUpdateById(
                                attemptId
                        )
                        .orElseThrow(
                                PinResetAttemptNotFoundException::new
                        );

        UUID requestedDeviceInstallId =
                loadDeviceInstallPort
                        .findDeviceInstallIdByInstallId(
                                command.getInstallId()
                        )
                        .orElseThrow(
                                PinResetMobileVerificationException::new
                        );

        verifySameDevice(
                attempt,
                requestedDeviceInstallId
        );

        verifyRegisteredMobile(
                attempt,
                command.getPhoneNumber()
        );

        Instant confirmedAt =
                timeProvider.now();

        if (!attempt.isResetAvailable(confirmedAt)) {
            throw new PinResetAttemptNotFoundException();
        }

        PinResetAttempt confirmedAttempt =
                attempt.confirmMobile(
                        confirmedAt
                );

        PinResetAttempt savedAttempt =
                pinResetAttemptRepository.save(
                        confirmedAttempt
                );

        return ConfirmPinResetMobileMapper.toResult(
                savedAttempt
        );
    }

    private static PinResetAttemptId parseAttemptId(
            String rawAttemptId
    ) {
        try {
            return PinResetAttemptId.fromString(
                    rawAttemptId
            );
        } catch (IllegalArgumentException exception) {
            throw new PinResetAttemptNotFoundException();
        }
    }

    private static void verifySameDevice(
            PinResetAttempt attempt,
            UUID requestedDeviceInstallId
    ) {
        if (!attempt
                .getDeviceInstallId()
                .equals(requestedDeviceInstallId)) {
            throw new PinResetMobileVerificationException();
        }
    }

    private void verifyRegisteredMobile(
            PinResetAttempt attempt,
            String phoneNumber
    ) {
        AuthUserSnapshot user =
                loadAuthUserPort
                        .findByPhoneNumber(
                                phoneNumber
                        )
                        .orElseThrow(
                                PinResetMobileVerificationException::new
                        );

        if (!attempt
                .getUserId()
                .equals(user.getUserId())) {
            throw new PinResetMobileVerificationException();
        }
    }
}
