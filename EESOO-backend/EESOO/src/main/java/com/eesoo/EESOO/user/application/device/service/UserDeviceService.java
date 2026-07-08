package com.eesoo.EESOO.user.application.device.service;

import org.springframework.stereotype.Service;

import com.eesoo.EESOO.user.application.device.command.StoreDeviceCommand;
import com.eesoo.EESOO.user.application.device.dto.DeviceCheckRequestDTO;
import com.eesoo.EESOO.user.application.device.dto.DeviceCheckResultDTO;
import com.eesoo.EESOO.user.application.device.dto.StoreDeviceResultDTO;
import com.eesoo.EESOO.user.application.device.dto.StoreDeviceRequestDTO;
import com.eesoo.EESOO.user.application.device.handler.DeviceCheckQueryHandler;
import com.eesoo.EESOO.user.application.device.handler.StoreDeviceCommandHandler;
import com.eesoo.EESOO.user.application.device.mapper.UserDeviceMapper;
import com.eesoo.EESOO.user.application.device.query.DeviceCheckQuery;

@Service
public class UserDeviceService {

    private final StoreDeviceCommandHandler storeDeviceCommandHandler;
    private final DeviceCheckQueryHandler deviceCheckQueryHandler;

    public UserDeviceService(
            StoreDeviceCommandHandler storeDeviceCommandHandler,
            DeviceCheckQueryHandler deviceCheckQueryHandler
    ) {
        this.storeDeviceCommandHandler = storeDeviceCommandHandler;
        this.deviceCheckQueryHandler = deviceCheckQueryHandler;
    }

    public StoreDeviceResultDTO storeDevice(StoreDeviceRequestDTO dto) {
        StoreDeviceCommand command = UserDeviceMapper.toCommand(dto);
        return storeDeviceCommandHandler.handle(command);
    }

    public DeviceCheckResultDTO checkDevice(DeviceCheckRequestDTO dto) {
        DeviceCheckQuery query = UserDeviceMapper.toQuery(dto);
        return deviceCheckQueryHandler.handle(query);
    }
}
