package com.eesoo.EESOO.user.application.device.handler;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.shared.application.cqrs.QueryHandler;
import com.eesoo.EESOO.user.application.device.dto.DeviceCheckResultDTO;
import com.eesoo.EESOO.user.application.device.query.DeviceCheckQuery;
import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;
import com.eesoo.EESOO.user.domain.model.entity.User;
import com.eesoo.EESOO.user.domain.model.entity.UserDeviceLink;
import com.eesoo.EESOO.user.domain.model.enums.LinkStatus;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.user.domain.repository.DeviceInstallRepository;
import com.eesoo.EESOO.user.domain.repository.UserDeviceLinkRepository;
import com.eesoo.EESOO.user.domain.repository.UserRepository;

@Service
public class DeviceCheckQueryHandler implements QueryHandler<DeviceCheckQuery, DeviceCheckResultDTO> {

    private final DeviceInstallRepository deviceInstallRepository;
    private final UserDeviceLinkRepository userDeviceLinkRepository;
    private final UserRepository userRepository;

    public DeviceCheckQueryHandler(
            DeviceInstallRepository deviceInstallRepository,
            UserDeviceLinkRepository userDeviceLinkRepository,
            UserRepository userRepository) {
        this.deviceInstallRepository = deviceInstallRepository;
        this.userDeviceLinkRepository = userDeviceLinkRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DeviceCheckResultDTO handle(DeviceCheckQuery query)  {

        DeviceId deviceId = toDeviceIdOrNull(query.getDeviceId());

        if(deviceId == null){
            return DeviceCheckResultDTO.notLinked("DEVICE_ID_MISSING");

        }

        Optional<DeviceInstall> deviceOptional = deviceInstallRepository.findByDeviceId(deviceId);
        if(deviceOptional.isEmpty()){
            return DeviceCheckResultDTO.notLinked("DEVICE_NOT_REGISTERED");
        }

        DeviceInstall device = deviceOptional.get();

        Optional<UserDeviceLink> activeLinkOptional = userDeviceLinkRepository.findActiveByDeviceInstallId(device.getId());

        if(activeLinkOptional.isEmpty()){
            return DeviceCheckResultDTO.notLinked("DEVICE_NOT_LINKED");
        }

        UserDeviceLink activeLink = activeLinkOptional.get();

        if(activeLink.getStatus() != LinkStatus.ACTIVE){
            return DeviceCheckResultDTO.notLinked("DEVICE_NOT_LINKED");
        }
        UserId userId = activeLink.getUserId();
        Optional<User> userOptional = userRepository.findById(userId.getValue());
        
        if (userOptional.isEmpty()) {
            return DeviceCheckResultDTO.notLinked("USER_NOT_FOUND");
        }

        User user = userOptional.get();

        return DeviceCheckResultDTO.linked(
            activeLink ,
            user
        );
    }

     private DeviceId toDeviceIdOrNull(String rawDeviceId) {
        if (rawDeviceId == null || rawDeviceId.trim().isEmpty()) {
            return null;
        }
        return DeviceId.of(rawDeviceId);
    }
}
