package com.eesoo.EESOO.user.application.register.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.eesoo.EESOO.shared.application.cqrs.CommandHandler;
import com.eesoo.EESOO.user.application.device.backend_outcome.DeviceLinkOutcome;
import com.eesoo.EESOO.user.application.device.service.DeviceLinkService;
import com.eesoo.EESOO.user.application.register.command.RegisterUserCommand;
import com.eesoo.EESOO.user.application.register.dto.RegisterUserResultDTO;
import com.eesoo.EESOO.user.application.register.service.UserCreationService;
import com.eesoo.EESOO.user.domain.model.entity.User;
import com.eesoo.EESOO.user.domain.model.enums.LinkedDeviceType;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;

@Component
public class RegisterUserCommandHandler implements CommandHandler<RegisterUserCommand, RegisterUserResultDTO> {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserCommandHandler.class);

    private final UserCreationService userCreationService;
    private final DeviceLinkService deviceLinkService;

    public RegisterUserCommandHandler(
            UserCreationService userCreationService,
            DeviceLinkService deviceLinkService
    ) {
        this.userCreationService = userCreationService;
        this.deviceLinkService = deviceLinkService;
    }

    @Override
    public RegisterUserResultDTO handle(RegisterUserCommand command) {
        User user = userCreationService.register(command);

        try{
            DeviceId deviceId = toDeviceIdOrNull(command.getDeviceId());

            DeviceLinkOutcome linkOutcome = deviceLinkService.tryLinkByDeviceIdOnly(
                    deviceId,
                    user.getId(),
                    LinkedDeviceType.MAIN_DEVICE
            );

            if (linkOutcome.isNotLinked()) {
                String reason = linkOutcome.getFailureReason()
                        .map(Enum::name)
                        .orElse("UNKNOWN_REASON");

                log.warn(
                        "Device linking not completed during registration. reason={}",
                        reason
                );
            }
        } catch (RuntimeException ex) {
            log.error(
                    "Device linking failed during registration.",
                    ex
            );
        }

        return RegisterUserResultDTO.from(user);
    }

    private DeviceId toDeviceIdOrNull(String rawDeviceId) {
        if (rawDeviceId == null || rawDeviceId.trim().isEmpty()) {
            return null;
        }
        return DeviceId.of(rawDeviceId);
    }
}
