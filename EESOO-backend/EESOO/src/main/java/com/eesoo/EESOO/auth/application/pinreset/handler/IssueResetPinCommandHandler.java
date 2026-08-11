package com.eesoo.EESOO.auth.application.pinreset.handler;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.auth.application.exception.PinResetAttemptNotFoundException;
import com.eesoo.EESOO.auth.application.pinreset.command.IssueResetPinCommand;
import com.eesoo.EESOO.auth.application.pinreset.dto.IssueResetPinResultDTO;
import com.eesoo.EESOO.auth.application.pinreset.mapper.IssueResetPinMapper;
import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.valueobject.PinResetAttemptId;
import com.eesoo.EESOO.auth.domain.port.LoadDeviceInstallPort;
import com.eesoo.EESOO.auth.domain.port.PinVerificationPort;
import com.eesoo.EESOO.auth.domain.port.ResetUserPinPort;
import com.eesoo.EESOO.auth.domain.repository.PinResetAttemptRepository;
import com.eesoo.EESOO.auth.domain.service.PinGenerator;
import com.eesoo.EESOO.shared.application.cqrs.CommandHandler;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;

@Component
public class IssueResetPinCommandHandler
        implements CommandHandler<
                IssueResetPinCommand,
                IssueResetPinResultDTO
        > {

    private static final int MAX_PIN_GENERATION_ATTEMPTS = 10;

    private final PinResetAttemptRepository pinResetAttemptRepository;
    private final LoadDeviceInstallPort loadDeviceInstallPort;
    private final PinGenerator pinGenerator;
    private final PinVerificationPort pinVerificationPort;
    private final ResetUserPinPort resetUserPinPort;
    private final TimeProvider timeProvider;

    public IssueResetPinCommandHandler(
            PinResetAttemptRepository pinResetAttemptRepository,
            LoadDeviceInstallPort loadDeviceInstallPort,
            PinGenerator pinGenerator,
            PinVerificationPort pinVerificationPort,
            ResetUserPinPort resetUserPinPort,
            TimeProvider timeProvider
    ) {
        this.pinResetAttemptRepository =
                pinResetAttemptRepository;

        this.loadDeviceInstallPort =
                loadDeviceInstallPort;

        this.pinGenerator =
                pinGenerator;

        this.pinVerificationPort =
                pinVerificationPort;

        this.resetUserPinPort =
                resetUserPinPort;

        this.timeProvider =
                timeProvider;
    }

    @Override
    @Transactional
    public IssueResetPinResultDTO handle(
            IssueResetPinCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "IssueResetPinCommand cannot be null"
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
                                PinResetAttemptNotFoundException::new
                        );

        verifySameDevice(
                attempt,
                requestedDeviceInstallId
        );

        Instant issuedAt =
                timeProvider.now();

        if (!attempt.isReadyForPinReset(issuedAt)) {
            throw new PinResetAttemptNotFoundException();
        }

        String newRawPin =
                generatePinDifferentFromCurrent(
                        attempt.getUserId()
                );

        resetUserPinPort.resetPin(
                attempt.getUserId(),
                newRawPin
        );

        PinResetAttempt issuedAttempt =
                attempt.markPinIssued(
                        issuedAt
                );

        PinResetAttempt savedAttempt =
                pinResetAttemptRepository.save(
                        issuedAttempt
                );

        return IssueResetPinMapper.toResult(
                savedAttempt,
                newRawPin
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
            throw new PinResetAttemptNotFoundException();
        }
    }

    private String generatePinDifferentFromCurrent(
            UUID userId
    ) {
        for (int attemptNumber = 0;
                attemptNumber < MAX_PIN_GENERATION_ATTEMPTS;
                attemptNumber++) {
            String generatedPin =
                    pinGenerator
                            .generateFourDigitPin();

            boolean matchesCurrentPin =
                    pinVerificationPort.verifyPin(
                            userId,
                            generatedPin
                    );

            if (!matchesCurrentPin) {
                return generatedPin;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a new PIN"
        );
    }
}
