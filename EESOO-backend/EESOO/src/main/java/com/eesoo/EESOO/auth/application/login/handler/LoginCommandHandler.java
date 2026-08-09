package com.eesoo.EESOO.auth.application.login.handler;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eesoo.EESOO.auth.application.exception.DeviceInstallationNotRegisteredException;
import com.eesoo.EESOO.auth.application.exception.DeviceLoginRejectedException;
import com.eesoo.EESOO.auth.application.exception.InvalidCredentialsException;
import com.eesoo.EESOO.auth.application.exception.PinLoginFailureException;
import com.eesoo.EESOO.auth.application.login.command.LoginUserCommand;
import com.eesoo.EESOO.auth.application.login.dto.LoginUserResultDTO;
import com.eesoo.EESOO.auth.application.login.mapper.LoginUserMapper;
import com.eesoo.EESOO.auth.application.login.service.AuthSessionCreationService;
import com.eesoo.EESOO.auth.application.pinreset.service.PinLoginFailureService;
import com.eesoo.EESOO.auth.domain.model.dto.AuthUserSnapshot;
import com.eesoo.EESOO.auth.domain.model.entity.AuthUser;
import com.eesoo.EESOO.auth.domain.model.entity.PinResetAttempt;
import com.eesoo.EESOO.auth.domain.model.enums.LoginDeviceLinkStatus;
import com.eesoo.EESOO.auth.domain.model.valueobject.TokenPair;
import com.eesoo.EESOO.auth.domain.port.LoadAuthUserPort;
import com.eesoo.EESOO.auth.domain.port.LoadDeviceInstallPort;
import com.eesoo.EESOO.auth.domain.port.LoginDeviceLinkPort;
import com.eesoo.EESOO.auth.domain.port.PinVerificationPort;
import com.eesoo.EESOO.auth.domain.repository.PinResetAttemptRepository;
import com.eesoo.EESOO.shared.application.cqrs.CommandHandler;
import com.eesoo.EESOO.shared.domain.time.TimeProvider;

@Component
public class LoginCommandHandler
        implements CommandHandler<
                LoginUserCommand,
                LoginUserResultDTO
        > {

    private static final Logger log =
            LoggerFactory.getLogger(
                    LoginCommandHandler.class
            );

    private final LoadAuthUserPort loadAuthUserPort;
    private final PinVerificationPort pinVerificationPort;
    private final LoadDeviceInstallPort loadDeviceInstallPort;
    private final LoginDeviceLinkPort loginDeviceLinkPort;
    private final AuthSessionCreationService authSessionCreationService;
    private final PinLoginFailureService pinLoginFailureService;
    private final PinResetAttemptRepository pinResetAttemptRepository;
    private final TimeProvider timeProvider;

    public LoginCommandHandler(
            LoadAuthUserPort loadAuthUserPort,
            PinVerificationPort pinVerificationPort,
            LoadDeviceInstallPort loadDeviceInstallPort,
            LoginDeviceLinkPort loginDeviceLinkPort,
            AuthSessionCreationService authSessionCreationService,
            PinLoginFailureService pinLoginFailureService,
            PinResetAttemptRepository pinResetAttemptRepository,
            TimeProvider timeProvider
    ) {
        this.loadAuthUserPort = loadAuthUserPort;
        this.pinVerificationPort = pinVerificationPort;
        this.loadDeviceInstallPort = loadDeviceInstallPort;
        this.loginDeviceLinkPort = loginDeviceLinkPort;
        this.authSessionCreationService =
                authSessionCreationService;
        this.pinLoginFailureService =
                pinLoginFailureService;
        this.pinResetAttemptRepository =
                pinResetAttemptRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    @Transactional
    public LoginUserResultDTO handle(
            LoginUserCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "Login command cannot be null"
            );
        }

        AuthUser authUser =
                loadAndValidateUser(command);

        UUID deviceInstallId =
                resolveDeviceInstallId(command);

        enforcePinResetStateBeforeVerification(
                authUser.getUserId(),
                deviceInstallId
        );

        verifyPinOrRecordFailure(
                authUser,
                deviceInstallId,
                command.getPin()
        );

        LoginDeviceLinkStatus deviceLinkStatus =
                attemptDeviceLink(
                        authUser,
                        deviceInstallId,
                        command.getDeviceId()
                );

        if (!deviceLinkStatus.allowsLogin()) {
            throw new DeviceLoginRejectedException(
                    deviceLinkStatus
            );
        }

        pinResetAttemptRepository
                .deleteByUserIdAndDeviceInstallId(
                        authUser.getUserId(),
                        deviceInstallId
                );

        TokenPair tokenPair =
                authSessionCreationService.createSession(
                        authUser.getUserId(),
                        deviceInstallId
                );

        return LoginUserMapper.toResult(
                authUser,
                tokenPair,
                deviceLinkStatus.isLinked(),
                deviceLinkStatus.isLinked()
                        ? null
                        : deviceLinkStatus.name()
        );
    }

    private AuthUser loadAndValidateUser(
            LoginUserCommand command
    ) {
        AuthUserSnapshot snapshot =
                loadAuthUserPort
                        .findByPhoneNumber(
                                command.getPhoneNumber()
                        )
                        .orElseThrow(
                                InvalidCredentialsException::new
                        );

        AuthUser authUser =
                AuthUser.of(
                        snapshot.getUserId(),
                        snapshot.getUsername(),
                        snapshot.getLoginPermission()
                );

        if (!authUser.canLogin()) {
            throw new InvalidCredentialsException();
        }

        return authUser;
    }

    private void enforcePinResetStateBeforeVerification(
            UUID userId,
            UUID deviceInstallId
    ) {
        Optional<PinResetAttempt> existingAttempt =
                pinResetAttemptRepository
                        .findForUpdateByUserIdAndDeviceInstallId(
                                userId,
                                deviceInstallId
                        );

        if (existingAttempt.isEmpty()) {
            return;
        }

        PinResetAttempt attempt =
                existingAttempt.get();

        if (attempt.hasPinBeenIssued()) {
            return;
        }

        Instant currentTime =
                timeProvider.now();

        if (attempt.isExpired(currentTime)) {
            pinResetAttemptRepository.deleteById(
                    attempt.getId()
            );

            return;
        }

        if (attempt.getStatus().isResetAvailable()
                || attempt.getStatus().isMobileConfirmed()) {
            throw new PinLoginFailureException(
                    attempt
            );
        }
    }

    private void verifyPinOrRecordFailure(
            AuthUser authUser,
            UUID deviceInstallId,
            String submittedPin
    ) {
        boolean pinMatches =
                pinVerificationPort.verifyPin(
                        authUser.getUserId(),
                        submittedPin
                );

        if (pinMatches) {
            return;
        }

        PinResetAttempt attempt =
                pinLoginFailureService
                        .recordFailedPin(
                                authUser.getUserId(),
                                deviceInstallId
                        );

        throw new PinLoginFailureException(
                attempt
        );
    }

    private UUID resolveDeviceInstallId(
            LoginUserCommand command
    ) {
        return loadDeviceInstallPort
                .findDeviceInstallIdByInstallId(
                        command.getInstallId()
                )
                .orElseThrow(
                        DeviceInstallationNotRegisteredException::new
                );
    }

    private LoginDeviceLinkStatus attemptDeviceLink(
            AuthUser authUser,
            UUID deviceInstallId,
            String deviceId
    ) {
        try {
            LoginDeviceLinkStatus linkStatus =
                    loginDeviceLinkPort.attemptLink(
                            authUser.getUserId(),
                            deviceInstallId,
                            deviceId
                    );

            log.info(
                    "Login device-link result. userId={}, deviceInstallId={}, status={}",
                    authUser.getUserId(),
                    deviceInstallId,
                    linkStatus
            );

            return linkStatus;

        } catch (RuntimeException exception) {
            log.error(
                    "Unexpected device-linking failure. userId={}, deviceInstallId={}, deviceId={}",
                    authUser.getUserId(),
                    deviceInstallId,
                    deviceId,
                    exception
            );

            throw exception;
        }
    }
}
