package com.eesoo.EESOO.auth.application.login.handler;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.eesoo.EESOO.auth.application.login.command.LoginUserCommand;
import com.eesoo.EESOO.auth.domain.model.entity.AuthUser;
import com.eesoo.EESOO.auth.domain.model.enums.LoginDeviceLinkStatus;
import com.eesoo.EESOO.auth.domain.port.LoadAuthUserPort;
import com.eesoo.EESOO.auth.domain.port.LoadDeviceInstallPort;
import com.eesoo.EESOO.auth.domain.port.LoginDeviceLinkPort;
import com.eesoo.EESOO.auth.domain.port.PinVerificationPort;
import com.eesoo.EESOO.shared.application.cqrs.CommandHandler;

@Component
public class LoginCommandHandler
        implements CommandHandler<
                LoginUserCommand,
                Optional<AuthUser>
        > {

    private static final Logger log =
            LoggerFactory.getLogger(
                    LoginCommandHandler.class
            );

    private final LoadAuthUserPort loadAuthUserPort;
    private final PinVerificationPort pinVerificationPort;
    private final LoadDeviceInstallPort loadDeviceInstallPort;
    private final LoginDeviceLinkPort loginDeviceLinkPort;

    public LoginCommandHandler(
            LoadAuthUserPort loadAuthUserPort,
            PinVerificationPort pinVerificationPort,
            LoadDeviceInstallPort loadDeviceInstallPort,
            LoginDeviceLinkPort loginDeviceLinkPort
    ) {
        this.loadAuthUserPort =
                loadAuthUserPort;

        this.pinVerificationPort =
                pinVerificationPort;

        this.loadDeviceInstallPort =
                loadDeviceInstallPort;

        this.loginDeviceLinkPort =
                loginDeviceLinkPort;
    }

    @Override
    public Optional<AuthUser> handle(
            LoginUserCommand command
    ) {
        Optional<AuthUser> authenticatedUser =
                authenticateUser(command);

        if (authenticatedUser.isEmpty()) {
            return Optional.empty();
        }

        AuthUser authUser =
                authenticatedUser.get();

        Optional<UUID> foundDeviceInstallId =
                loadDeviceInstallPort
                        .findDeviceInstallIdByInstallId(
                                command.getInstallId()
                        );

        /*
         * A registered DeviceInstall is required before
         * an AuthSession can be created.
         */
        if (foundDeviceInstallId.isEmpty()) {
            log.warn(
                    "Login blocked because device installation is not registered. userId={}, installId={}",
                    authUser.getUserId(),
                    command.getInstallId()
            );

            return Optional.empty();
        }

        UUID deviceInstallId =
                foundDeviceInstallId.get();

        attemptOptionalDeviceLink(
                authUser,
                deviceInstallId,
                command.getDeviceId()
        );

        return Optional.of(authUser);
    }

    private Optional<AuthUser> authenticateUser(
            LoginUserCommand command
    ) {
        return loadAuthUserPort
                .findByPhoneNumber(
                        command.getPhoneNumber()
                )
                .map(snapshot ->
                        AuthUser.of(
                                snapshot.getUserId(),
                                snapshot.getUsername(),
                                snapshot.getLoginPermission()
                        )
                )
                .filter(AuthUser::canLogin)
                .filter(authUser ->
                        pinVerificationPort.verifyPin(
                                authUser.getUserId(),
                                command.getPin()
                        )
                );
    }

    private void attemptOptionalDeviceLink(
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

        } catch (RuntimeException exception) {
            /*
             * Device linking is optional.
             * A linking failure does not cancel login.
             */
            log.warn(
                    "Optional device linking failed during login. userId={}, deviceInstallId={}, deviceId={}",
                    authUser.getUserId(),
                    deviceInstallId,
                    deviceId,
                    exception
            );
        }
    }
}