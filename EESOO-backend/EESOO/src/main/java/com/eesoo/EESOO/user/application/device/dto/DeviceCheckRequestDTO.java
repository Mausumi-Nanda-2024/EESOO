package com.eesoo.EESOO.user.application.device.dto;

public class DeviceCheckRequestDTO {

    private String deviceId;

    public DeviceCheckRequestDTO(String deviceId){
        this.deviceId = deviceId;
    }

    public String getDeviceId(){
        return deviceId;
    }
    
}
