package com.eesoo.EESOO.auth.application.login.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eesoo.EESOO.auth.application.login.command.LoginUserCommand;
import com.eesoo.EESOO.auth.domain.model.dto.AuthUserSnapshot;
import com.eesoo.EESOO.auth.domain.model.entity.AuthUser;
import com.eesoo.EESOO.auth.domain.model.enums.LoginPermission;
import com.eesoo.EESOO.auth.domain.port.LoadAuthUserPort;
import com.eesoo.EESOO.auth.domain.model.enums.LoginDeviceLinkStatus;
import com.eesoo.EESOO.auth.domain.port.LoginDeviceLinkPort;
import com.eesoo.EESOO.auth.domain.port.PinVerificationPort;

@ExtendWith(MockitoExtension.class)
class LoginCommandHandlerTest {

    @Mock
    private LoadAuthUserPort loadAuthUserPort;

    @Mock
    private PinVerificationPort pinVerificationPort;

    @Mock
    private LoginDeviceLinkPort loginDeviceLinkPort;

    private LoginCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LoginCommandHandler(
                loadAuthUserPort,
                pinVerificationPort,
                loginDeviceLinkPort
        );
    }

    @Test
    void shouldReturnAuthUserWhenPhoneNumberAndPinAreCorrect() {
        String phoneNumber = "9876543210";
        String pin = "1234";
        UUID userId = UUID.randomUUID();

        LoginUserCommand command = new LoginUserCommand(
                phoneNumber,
                pin,
                "device-123",
                "install-456"
        );

        AuthUserSnapshot snapshot = new AuthUserSnapshot(
                userId,
                "nehan123",
                LoginPermission.ELIGIBLE
        );

        when(loadAuthUserPort.findByPhoneNumber(phoneNumber))
                .thenReturn(Optional.of(snapshot));

        when(pinVerificationPort.verifyPin(userId, pin))
                .thenReturn(true);

        when(loginDeviceLinkPort.attemptLink(
                userId,
                "device-123"
        )).thenReturn(LoginDeviceLinkStatus.LINKED);

        Optional<AuthUser> result = handler.handle(command);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        assertEquals("nehan123", result.get().getUsername());
        assertTrue(result.get().canLogin());

        verify(loadAuthUserPort)
                .findByPhoneNumber(phoneNumber);

        verify(pinVerificationPort)
                .verifyPin(userId, pin);

        verify(loginDeviceLinkPort)
                .attemptLink(userId, "device-123");
    }

    @Test
    void shouldReturnAuthUserWhenDeviceLinkFailsBecauseDeviceIsNotRegistered() {
        String phoneNumber = "9876543210";
        String pin = "1234";
        UUID userId = UUID.randomUUID();

        LoginUserCommand command = new LoginUserCommand(
                phoneNumber,
                pin,
                "device-123",
                "install-456"
        );

        AuthUserSnapshot snapshot = new AuthUserSnapshot(
                userId,
                "nehan123",
                LoginPermission.ELIGIBLE
        );

        when(loadAuthUserPort.findByPhoneNumber(phoneNumber))
                .thenReturn(Optional.of(snapshot));

        when(pinVerificationPort.verifyPin(userId, pin))
                .thenReturn(true);

        when(loginDeviceLinkPort.attemptLink(
                userId,
                "device-123"
        )).thenReturn(LoginDeviceLinkStatus.DEVICE_NOT_REGISTERED);

        Optional<AuthUser> result = handler.handle(command);

        assertTrue(result.isPresent());

        verify(loginDeviceLinkPort)
                .attemptLink(userId, "device-123");
    }

    @Test
    void shouldReturnEmptyWhenPinIsIncorrect() {
        String phoneNumber = "9876543210";
        String pin = "1234";
        UUID userId = UUID.randomUUID();

        LoginUserCommand command = new LoginUserCommand(
                phoneNumber,
                pin,
                "device-123",
                "install-456"
        );

        AuthUserSnapshot snapshot = new AuthUserSnapshot(
                userId,
                "nehan123",
                LoginPermission.ELIGIBLE
        );

        when(loadAuthUserPort.findByPhoneNumber(phoneNumber))
                .thenReturn(Optional.of(snapshot));

        when(pinVerificationPort.verifyPin(userId, pin))
                .thenReturn(false);

        Optional<AuthUser> result = handler.handle(command);

        assertTrue(result.isEmpty());

        verify(loadAuthUserPort)
                .findByPhoneNumber(phoneNumber);

        verify(pinVerificationPort)
                .verifyPin(userId, pin);

        verifyNoInteractions(loginDeviceLinkPort);
    }

    @Test
    void shouldReturnEmptyAndNotVerifyPinWhenPhoneNumberDoesNotExist() {
        String phoneNumber = "9876543210";

        LoginUserCommand command = new LoginUserCommand(
                phoneNumber,
                "1234",
                "device-123",
                "install-456"
        );

        when(loadAuthUserPort.findByPhoneNumber(phoneNumber))
                .thenReturn(Optional.empty());

        Optional<AuthUser> result = handler.handle(command);

        assertTrue(result.isEmpty());

        verify(loadAuthUserPort)
                .findByPhoneNumber(phoneNumber);

        verifyNoInteractions(pinVerificationPort);
        verifyNoInteractions(loginDeviceLinkPort);
    }

    @Test
    void shouldNotAttemptDeviceLinkWhenUserCannotLogin() {
        String phoneNumber = "9876543210";
        String pin = "1234";
        UUID userId = UUID.randomUUID();

        LoginUserCommand command = new LoginUserCommand(
                phoneNumber,
                pin,
                "device-123",
                "install-456"
        );

        AuthUserSnapshot snapshot = new AuthUserSnapshot(
                userId,
                "nehan123",
                LoginPermission.PENDING_VERIFICATION
        );

        when(loadAuthUserPort.findByPhoneNumber(phoneNumber))
                .thenReturn(Optional.of(snapshot));

        when(pinVerificationPort.verifyPin(userId, pin))
                .thenReturn(true);

        Optional<AuthUser> result = handler.handle(command);

        assertTrue(result.isEmpty());

        verify(pinVerificationPort)
                .verifyPin(userId, pin);

        verifyNoInteractions(loginDeviceLinkPort);
    }
}
